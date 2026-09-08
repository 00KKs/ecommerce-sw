package github.sangwook.ecommerce.order.application;

import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse.AddressResponse;
import github.sangwook.ecommerce.order.domain.*;
import github.sangwook.ecommerce.order.domain.ProductSnapshots.ProductSnapshot;
import github.sangwook.ecommerce.order.exception.OrderFailedException;
import github.sangwook.ecommerce.order.port.AddressPort;
import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.ProductPort;
import github.sangwook.ecommerce.order.port.StockPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.stock.OutOfStockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Map.Entry;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceOrderUseCase {

    private final AddressPort addressPort;
    private final StockPort stockPort;
    private final ProductPort productPort;
    private final PaymentPort paymentPort;

    private final OrderRepository orderRepository;

    private final TransactionTemplate transactionTemplate;


    public PlaceOrderResponse placeOrder(Long memberId, Long addressId, Map<Long, Integer> skuIdQuantityMap) {
        AddressSnapshot addressSnapshot = addressPort.getAddressSnapshot(memberId, addressId);
        ProductSnapshots productSnapshots = productPort.getProductSnapshots(skuIdQuantityMap);

        Order order = transactionTemplate.execute(status -> {
            try {
                for (Entry<Long, Integer> entry : skuIdQuantityMap.entrySet()) {
                    stockPort.deduct(entry.getKey(), entry.getValue());
                }

                Order newOrder = createOrder(productSnapshots, addressSnapshot);
                return orderRepository.save(newOrder);

            } catch (OutOfStockException e) {
                log.warn("재고 부족으로 주문 실패 - memberId={}, skuId={}, 요청수량={}, 가용재고={}", memberId,
                    e.getSkuId(), e.getRequestQuantity(), e.getAvailableQuantity());
                throw new OrderFailedException(e);
            } catch (Exception e) {
                throw new OrderFailedException(e);
            }
        });

        PaymentResult paymentResult = paymentPort.processPayment(order.getId(), order.getTotalPrice());
        switch (paymentResult) {
            case PaymentResult.SUCCESS(String paymentKey) -> {
                Order confirmed = transactionTemplate.execute(status -> {
                    Order getOrder = getByIdWithItems(order.getId());
                    getOrder.confirm();
                    return orderRepository.save(getOrder);
                });

                return new PlaceOrderResponse(
                        order.getId(),
                        OrderDisplayStatus.CONFIRMED,
                        confirmed.getTotalPrice(),
                        paymentKey,
                        confirmed.getOrderItems().stream().map(oi -> new PlaceOrderResponse.ItemResponse(oi.getProductName(), oi.getOptionName(), oi.getUnitPrice(), oi.getQuantity())).toList(),
                        new AddressResponse(addressSnapshot.getRecipientName(), addressSnapshot.getRecipientPhone(), addressSnapshot.getAddress(),addressSnapshot.getDeliveryRequest())
                );
            }

            case PaymentResult.PAYMENT_FAILED(PaymentResult.PaymentFailedStage stage, boolean retryable) -> {
                if (retryable) { //재시도 가능한 실패 시 사용자의 재결제를 유도한다
                    Order failed = getByIdWithItems(order.getId());
                    return new PlaceOrderResponse(
                        failed.getId(),
                        OrderDisplayStatus.PAYMENT_PENDING,
                        failed.getTotalPrice(),
                        null,
                        failed.getOrderItems().stream().map(oi -> new PlaceOrderResponse.ItemResponse(oi.getProductName(), oi.getOptionName(), oi.getUnitPrice(), oi.getQuantity())).toList(),
                        new AddressResponse(addressSnapshot.getRecipientName(), addressSnapshot.getRecipientPhone(), addressSnapshot.getAddress(),addressSnapshot.getDeliveryRequest())
                    );
                }

                Order failed = transactionTemplate.execute(status -> {
                    for (Entry<Long, Integer> entry : skuIdQuantityMap.entrySet()) {
                        stockPort.recover(entry.getKey(), entry.getValue());
                    }

                    Order failedOrder = getByIdWithItems(order.getId());
                    failedOrder.paymentFailed();
                    return orderRepository.save(failedOrder);
                });

                switch (stage) {
                    case PaymentResult.PaymentFailedStage.PAYMENT_INITIATE() -> {
                        return new PlaceOrderResponse(
                                order.getId(),
                                OrderDisplayStatus.FAILED,
                                failed.getTotalPrice(),
                                null, //실패 단계에 따라 paymentKey가 있을수도 없을수도 있다.
                                failed.getOrderItems().stream().map(oi -> new PlaceOrderResponse.ItemResponse(oi.getProductName(), oi.getOptionName(), oi.getUnitPrice(), oi.getQuantity())).toList(),
                                new AddressResponse(addressSnapshot.getRecipientName(), addressSnapshot.getRecipientPhone(), addressSnapshot.getAddress(),addressSnapshot.getDeliveryRequest())
                        );
                    }
                    case PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(String paymentKey) -> {
                        return new PlaceOrderResponse(
                                order.getId(),
                                OrderDisplayStatus.FAILED,
                                failed.getTotalPrice(),
                                paymentKey,
                                failed.getOrderItems().stream().map(oi -> new PlaceOrderResponse.ItemResponse(oi.getProductName(), oi.getOptionName(), oi.getUnitPrice(), oi.getQuantity())).toList(),
                                new AddressResponse(addressSnapshot.getRecipientName(), addressSnapshot.getRecipientPhone(), addressSnapshot.getAddress(),addressSnapshot.getDeliveryRequest())
                        );
                    }
                }
            }
        }
    }

    private @NonNull Order createOrder(ProductSnapshots productSnapshots, AddressSnapshot addressSnapshot) {
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
            order.addOrderItem(item.getProductName(), item.getOptionName(), item.getUnitPrice(), item.getQuantity());
        }
        return order;
    }

    private Order getByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id).orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다."));
    }
}
