package github.sangwook.ecommerce.order.port;

import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import java.util.Optional;

public interface PaymentPort {

    Long prepare(Long orderId, int amount);

    PaymentResult processPayment(Long paymentId);

    Optional<PaymentResult> findSettledOutcome(Long orderId);
}
