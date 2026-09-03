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
        //배송지를 확인하고, 스냅샷을 가져온다
        AddressSnapshot addressSnapshot = addressPort.getAddressSnapshot(memberId, addressId);

        //상품 정보의 스냅샷을 가져온다
        ProductSnapshots productSnapshots = productPort.getProductSnapshots(skuIdQuantityMap);

        //재고를 줄인다
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

        //일단 재고까지 줄이는 것에 성공하면 주문의 결제 대기 상태까지는 완료되었고 커밋하면됨 - 트랜잭션 1 종료

        //PG사에 결제 요청을 한다 - 트랜잭션 외부

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

            case PaymentResult.FAILED() -> {
                //재고 되돌리기
                return null;
            }

            case PaymentResult.UNKNOWN() -> {
                //응답 반환
                //주문 성공여부 확인, 여부에 맞게 처리
                return null;
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
