package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public PaymentInitiateResult initiatePayment(Long orderId, Integer amount) {
        return new PaymentInitiateResult.SUCCESS(UUID.randomUUID().toString());
    }

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        return new PaymentConfirmResult.SUCCESS();
    }
}
