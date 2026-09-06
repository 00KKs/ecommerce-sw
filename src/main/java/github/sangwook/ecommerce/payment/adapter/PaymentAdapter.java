package github.sangwook.ecommerce.payment.adapter;

import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import java.util.UUID;

import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import github.sangwook.ecommerce.payment.application.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
            case PaymentInitiateResult.FAILED(String reasonCode, String reasonMessage) -> {
                //재시도
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_INITIATE());
            }
            case PaymentInitiateResult.UNKNOWN(Throwable cause) -> {
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_INITIATE());
            }
        }

        UUID paymentIdempotencyKey = UUID.randomUUID();
        Long paymentId = paymentService.ready(orderId, amount, paymentKey, paymentIdempotencyKey);

        //결제 대기 상태 저장
        switch (paymentGateway.confirmPayment(paymentKey, orderId, amount, paymentIdempotencyKey)) {
            case PaymentConfirmResult.SUCCESS() -> {
                paymentService.success(paymentId);
                return new PaymentResult.SUCCESS(paymentKey);
            }
            case PaymentConfirmResult.FAILED(String reasonCode, String reasonMessage) -> {
                paymentService.aborted(paymentId);
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey));
            }
            case PaymentConfirmResult.UNKNOWN(Throwable cause) -> {
                //재확인 후 기록
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey));
            }
        }
    }
}
