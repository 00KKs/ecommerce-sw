package github.sangwook.ecommerce.order.port;

import github.sangwook.ecommerce.order.port.dto.PaymentResult;

public interface PaymentPort {

    Long prepare(Long orderId, int amount);

    PaymentResult processPayment(Long paymentId);
}
