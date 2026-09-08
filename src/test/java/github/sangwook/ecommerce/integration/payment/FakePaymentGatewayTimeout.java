package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.exception.PaymentConfirmAmbiguousException;
import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import java.util.UUID;

public class FakePaymentGatewayTimeout implements PaymentGateway {

    @Override
    public PaymentInitiateResult initiatePayment(Long orderId, Integer amount) {
        return new PaymentInitiateResult.UNKNOWN(new IllegalStateException("결제 요청 타임아웃"));
    }

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        return new PaymentConfirmResult.UNKNOWN(new PaymentConfirmAmbiguousException("결제 승인 타임아웃"));
    }
}
