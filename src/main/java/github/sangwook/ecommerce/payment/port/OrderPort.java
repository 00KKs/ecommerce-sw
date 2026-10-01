package github.sangwook.ecommerce.payment.port;

import github.sangwook.ecommerce.order.port.dto.PaymentResult;

public interface OrderPort {
    void applyPaymentOutcome(PaymentResult paymentResult, Long orderId);
}
