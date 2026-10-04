package github.sangwook.ecommerce.order.application;

import github.sangwook.ecommerce.order.port.dto.AddressSnapshot;
import github.sangwook.ecommerce.order.domain.Order;
import github.sangwook.ecommerce.order.domain.OrderItem;
import github.sangwook.ecommerce.order.domain.OrderStatus;
import github.sangwook.ecommerce.order.port.dto.ProductSnapshots;
import github.sangwook.ecommerce.order.port.dto.ProductSnapshots.ProductSnapshot;
import github.sangwook.ecommerce.order.port.dto.ShippingAddress;
import github.sangwook.ecommerce.order.exception.InsufficientStockException;
import github.sangwook.ecommerce.order.exception.OrderFailedException;
import github.sangwook.ecommerce.order.port.StockPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TransactionTemplate transactionTemplate;

    private final StockPort stockPort;

    public Order createOrder(Long memberId, AddressSnapshot addressSnapshot, ProductSnapshots productSnapshots, Map<Long, Integer> skuIdQuantityMap) {
        return transactionTemplate.execute(status -> {
            try {
                for (Entry<Long, Integer> entry : skuIdQuantityMap.entrySet()) {
                    stockPort.deduct(entry.getKey(), entry.getValue());
                }

                Order newOrder = create(productSnapshots, addressSnapshot);
                return orderRepository.save(newOrder);

            } catch (InsufficientStockException e) {
                log.warn("재고 부족으로 주문 실패 - memberId={}, skuId={}, 요청수량={}, 가용재고={}", memberId, e.getSkuId(), e.getRequestQuantity(), e.getAvailableQuantity());
                throw new OrderFailedException(e);
            } catch (Exception e) {
                throw new OrderFailedException(e);
            }
        });
    }

    @Transactional
    public Order confirm(Long orderId) {
        Order order = getByIdWithItems(orderId);
        if (order.getStatus() == OrderStatus.CONFIRMED) return order;
        order.confirm();
        return orderRepository.save(order);
    }

    @Transactional
    public Order fail(Long orderId) {
        Order order = getByIdWithItems(orderId);
        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) return order;
        order.paymentFailed();
        for (OrderItem item : order.getOrderItems()) {
            stockPort.recover(item.getSkuId(), item.getQuantity());
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order applyPaymentOutcome(PaymentResult paymentResult, Long orderId) {
        return switch (paymentResult) {
            case PaymentResult.CONFIRMED ignored -> confirm(orderId);
            case PaymentResult.FAILED ignored -> fail(orderId);
            case PaymentResult.PENDING ignored -> getByIdWithItems(orderId);
        };
    }

    private @NonNull Order create(ProductSnapshots productSnapshots, AddressSnapshot addressSnapshot) {
        Order order = new Order(
            OrderStatus.PAYMENT_PENDING,
            productSnapshots.calculateTotalPrice(),
            new ShippingAddress(
                addressSnapshot.getRecipientName(),
                addressSnapshot.getRecipientPhone(),
                addressSnapshot.getAddress(),
                addressSnapshot.getDeliveryRequest()
            )
        );

        for (ProductSnapshot item : productSnapshots.getItems()) {
            order.addOrderItem(item.getSkuId(), item.getProductName(), item.getOptionName(), item.getUnitPrice(), item.getQuantity());
        }
        return order;
    }

    private Order getByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id).orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다."));
    }
}
