package github.sangwook.ecommerce.order.application;

import github.sangwook.ecommerce.order.domain.Order;
import github.sangwook.ecommerce.order.domain.OrderStatus;
import github.sangwook.ecommerce.order.port.PaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReconciliationScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentPort paymentPort;

    @Scheduled(fixedDelay = 60_000)
    public void reconcilePendingOrders() {
        orderRepository.findAllByStatus(OrderStatus.PAYMENT_PENDING).forEach(this::reconcile);
    }

    private void reconcile(Order order) {
        Long orderId = order.getId();
        paymentPort.findSettledOutcome(orderId).ifPresent(outcome -> {
            try {
                orderService.applyPaymentOutcome(outcome, orderId);
                log.info("주문-결제 상태 보정 완료. orderId={}, outcome={}", orderId, outcome);
            } catch (RuntimeException e) {
                log.error("주문-결제 상태 보정 실패. orderId={}, outcome={}", orderId, outcome, e);
            }
        });
    }

}
