package github.sangwook.ecommerce.payment.adapter;

import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.order.port.dto.PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM;
import java.util.UUID;

import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import github.sangwook.ecommerce.payment.application.PaymentService;
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
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), false);
            }
        }
    }
}
