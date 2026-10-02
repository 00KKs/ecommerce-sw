package github.sangwook.ecommerce.order.adapter;

import github.sangwook.ecommerce.order.application.OrderService;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.payment.port.OrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OrderAdapter implements OrderPort {

    private final OrderService orderService;

    @Override
    public void applyPaymentOutcome(PaymentResult paymentResult, Long orderId) {
        orderService.applyPaymentOutcome(paymentResult, orderId);
    }
}
