package github.sangwook.ecommerce.order.adapter;

import github.sangwook.ecommerce.order.application.OrderService;
import github.sangwook.ecommerce.payment.port.OrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OrderAdapter implements OrderPort {

    private final OrderService orderService;

    @Override
    public void confirmOrder(Long orderId) {
        orderService.confirm(orderId);
    }

    @Override
    public void failOrder(Long orderId) {
        orderService.fail(orderId);
    }
}
