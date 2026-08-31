package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.PaymentGateway;
import github.sangwook.ecommerce.payment.infrastructure.PaymentStatus;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public String initiatePayment(Long orderId, Integer amount) {
        return UUID.randomUUID().toString();
    }

    @Override
    public PaymentStatus confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        return PaymentStatus.DONE;
    }
}
