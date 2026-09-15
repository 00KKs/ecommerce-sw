package github.sangwook.ecommerce.payment.adapter;

import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.order.port.dto.PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM;
import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.application.PaymentService;
import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentLookupResult;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class PaymentAdapter implements PaymentPort {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    //이 요청 자체도 중복되어 들어올 수 있으므로 주문하기에서도 멱등성 보장이 필요하다
    @Override
    public PaymentResult processPayment(Long orderId, int amount) {

        String paymentKey;
        switch (paymentGateway.initiatePayment(orderId, amount)) {
            case PaymentInitiateResult.SUCCESS(String key) -> paymentKey = key;
            case PaymentInitiateResult.FAILED(String reasonCode, String reasonMessage, boolean retryable) -> {
                //재시도
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_INITIATE(), retryable);
            }
            case PaymentInitiateResult.UNKNOWN(Throwable cause) -> {
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_INITIATE(), false);
            }
        }

        UUID paymentIdempotencyKey = UUID.randomUUID();
        Long paymentId = paymentService.ready(orderId, amount, paymentKey, paymentIdempotencyKey);

        switch (paymentGateway.confirmPayment(paymentKey, orderId, amount, paymentIdempotencyKey)) {
            case PaymentConfirmResult.SUCCESS(Long paymentOrderId, int paymentAmount) -> {
                if (!paymentOrderId.equals(orderId) || paymentAmount != amount) {
                    log.error("PG 응답 값 불일치. orderId 기대={}, 실제={}, amount 기대={}, 실제={}", orderId, paymentOrderId, amount, paymentAmount);
                    paymentService.aborted(paymentId);
                    return new PaymentResult.PAYMENT_FAILED(new PAYMENT_CONFIRM(paymentKey), false);
                }
                paymentService.success(paymentId);
                return new PaymentResult.SUCCESS(paymentKey);
            }
            case PaymentConfirmResult.FAILED(String reasonCode, String reasonMessage, boolean retryable) -> {
                paymentService.aborted(paymentId);
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), retryable);
            }
            case PaymentConfirmResult.UNKNOWN(Throwable cause) -> {
                //재확인 후 기록
                switch (paymentGateway.lookupPayment(paymentKey)) {
                    case PaymentLookupResult.DONE(int doneAmount, OffsetDateTime approvedAt) -> {
                        if (doneAmount != amount) {
                            log.error("재확인 결과 금액 불일치. 기대={}, 실제={}, paymentKey={}", amount, doneAmount, paymentKey);
                            return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), false);
                        }
                        log.info("재확인 결과 승인 완료 확인, 동기화 진행. paymentKey={}", paymentKey);
                        paymentService.success(paymentId);
                        return new PaymentResult.SUCCESS(paymentKey);
                    }
                    case PaymentLookupResult.ABORTED() -> {
                        log.info("재확인 결과 승인 실패 확인. paymentKey={}", paymentKey);
                        paymentService.aborted(paymentId);
                        return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), false);
                    }
                    case PaymentLookupResult.NOT_FOUND() -> {
                        log.info("재확인 결과 PG에 내역 없음. paymentKey={}", paymentKey);
                        paymentService.aborted(paymentId); //TODO 재검토 필요
                        return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), true);
                    }
                    case PaymentLookupResult.READY() -> {
                        log.info("재확인 결과 아직 미승인. paymentKey={}", paymentKey);
                        paymentService.aborted(paymentId);
                        return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), true);
                    }
                    case PaymentLookupResult.CANCELED(int canceledAmount, OffsetDateTime approvedAt, OffsetDateTime canceledAt) -> {
                        log.error("예상치 못한 상태(CANCELED) 확인. 수동 확인 필요. paymentKey={}, approvedAt={}, canceledAt={}", paymentKey, approvedAt, canceledAt);
                        return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), false);
                    }
                }
            }
        }
    }
}
