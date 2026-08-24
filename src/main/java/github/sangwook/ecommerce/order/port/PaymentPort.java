package github.sangwook.ecommerce.order.port;

import github.sangwook.ecommerce.order.port.dto.PaymentResult;

public interface PaymentPort {

    PaymentResult processPayment(Long orderId, int amount);
}
