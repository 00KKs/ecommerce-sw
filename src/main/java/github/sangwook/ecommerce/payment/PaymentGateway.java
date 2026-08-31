package github.sangwook.ecommerce.payment;

import github.sangwook.ecommerce.payment.infrastructure.PaymentStatus;
import java.util.UUID;

public interface PaymentGateway {

    String initiatePayment(Long orderId, Integer amount);

    PaymentStatus confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey);

}
