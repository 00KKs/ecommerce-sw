package github.sangwook.ecommerce.payment;

import java.util.UUID;

public interface PaymentGateway {

    PaymentInitiateResult initiatePayment(Long orderId, Integer amount);

    PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey);

}
