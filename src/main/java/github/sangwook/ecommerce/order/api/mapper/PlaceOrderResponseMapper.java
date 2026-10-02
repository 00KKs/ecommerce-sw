package github.sangwook.ecommerce.order.api.mapper;

import github.sangwook.ecommerce.order.api.dto.OrderDisplayStatus;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse.AddressResponse;
import github.sangwook.ecommerce.order.domain.AddressSnapshot;
import github.sangwook.ecommerce.order.domain.Order;
import github.sangwook.ecommerce.order.domain.OrderStatus;
import jakarta.annotation.Nullable;

public class PlaceOrderResponseMapper {

    public static PlaceOrderResponse buildResponse(Order order, @Nullable String paymentKey, AddressSnapshot addressSnapshot) {
        return new PlaceOrderResponse(
            order.getId(),
            toDisplayStatus(order.getStatus()),
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

    private static OrderDisplayStatus toDisplayStatus(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> OrderDisplayStatus.CONFIRMED;
            case PAYMENT_FAILED -> OrderDisplayStatus.FAILED;
            case PAYMENT_PENDING -> OrderDisplayStatus.PENDING_CONFIRMATION;
        };
    }
}
