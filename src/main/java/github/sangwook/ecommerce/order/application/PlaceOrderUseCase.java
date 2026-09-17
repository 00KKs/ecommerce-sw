package github.sangwook.ecommerce.order.application;

import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse.AddressResponse;
import github.sangwook.ecommerce.order.domain.AddressSnapshot;
import github.sangwook.ecommerce.order.domain.Order;
import github.sangwook.ecommerce.order.domain.ProductSnapshots;
import github.sangwook.ecommerce.order.port.AddressPort;
import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.ProductPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import jakarta.annotation.Nullable;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceOrderUseCase {

    private final OrderService orderService;

    private final PaymentPort paymentPort;
    private final AddressPort addressPort;
    private final ProductPort productPort;

    public PlaceOrderResponse placeOrder(Long memberId, Long addressId, Map<Long, Integer> skuIdQuantityMap) {
        AddressSnapshot addressSnapshot = addressPort.getAddressSnapshot(memberId, addressId);
        ProductSnapshots productSnapshots = productPort.getProductSnapshots(skuIdQuantityMap);

        Order order = orderService.createOrder(memberId, addressSnapshot, productSnapshots, skuIdQuantityMap);
        Long orderId = order.getId();

        PaymentResult paymentResult = paymentPort.processPayment(orderId, order.getTotalPrice());
        switch (paymentResult) {
            case PaymentResult.SUCCESS(String paymentKey) -> {
                Order confirmed = orderService.confirm(orderId);
                return buildResponse(confirmed, paymentKey, OrderDisplayStatus.CONFIRMED, addressSnapshot);
            }

            case PaymentResult.PAYMENT_FAILED(PaymentResult.PaymentFailedStage stage, boolean retryable) -> {
                if (!retryable) {
                    Order failed = orderService.fail(orderId);
                    return buildFailedResponse(failed, OrderDisplayStatus.FAILED, stage, addressSnapshot);
                }
                return buildFailedResponse(order, OrderDisplayStatus.PENDING_CONFIRMATION, stage, addressSnapshot);
            }

            case PaymentResult.PAYMENT_UNKNOWN(String paymentKey) -> {
                return buildResponse(order, paymentKey, OrderDisplayStatus.PENDING_CONFIRMATION, addressSnapshot);
            }
        }
    }

    private PlaceOrderResponse buildFailedResponse(Order order, OrderDisplayStatus status, PaymentResult.PaymentFailedStage stage, AddressSnapshot addressSnapshot) {
        String paymentKey = switch (stage) {
            case PaymentResult.PaymentFailedStage.PAYMENT_INITIATE ignored -> null;
            case PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(String key) -> key;
        };

        return buildResponse(order, paymentKey, status, addressSnapshot);
    }

    private PlaceOrderResponse buildResponse(Order order, @Nullable String paymentKey, OrderDisplayStatus status, AddressSnapshot addressSnapshot) {
        return new PlaceOrderResponse(
            order.getId(),
            status,
            order.getTotalPrice(),
            paymentKey,
            order.getOrderItems().stream()
                .map(oi -> new PlaceOrderResponse.ItemResponse(
                    oi.getProductName(), oi.getOptionName(), oi.getUnitPrice(), oi.getQuantity()))
                .toList(),
            new AddressResponse(
                addressSnapshot.getRecipientName(),
                addressSnapshot.getRecipientPhone(),
                addressSnapshot.getAddress(),
                addressSnapshot.getDeliveryRequest())
        );
    }
}
