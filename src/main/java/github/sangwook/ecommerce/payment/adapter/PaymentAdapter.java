package github.sangwook.ecommerce.payment.adapter;

import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.payment.application.PaymentConfirmResolver;
import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.application.PaymentService;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class PaymentAdapter implements PaymentPort {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    private final PaymentConfirmResolver paymentConfirmResolver;

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
            case PaymentInitiateResult.UNKNOWN() -> {
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_INITIATE(), false);
            }
        }

        UUID paymentIdempotencyKey = UUID.randomUUID();
        Long paymentId = paymentService.ready(orderId, amount, paymentKey, paymentIdempotencyKey);

        return paymentConfirmResolver.resolve(paymentKey, orderId, amount, paymentIdempotencyKey, paymentId);
    }
}
