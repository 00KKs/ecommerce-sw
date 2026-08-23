package github.sangwook.ecommerce.order.api.dto;

import github.sangwook.ecommerce.order.application.OrderDisplayStatus;
import java.util.List;

public record PlaceOrderResponse(
    Long orderId,
    OrderDisplayStatus status,
    Integer totalPrice,
    String paymentKey,
    List<ItemResponse> items,
    AddressResponse shippingAddress
) {
    public record ItemResponse(
        String productName,
        String optionName,
        int unitPrice,
        int quantity
    ) { }

    public record AddressResponse(
        String recipientName,
        String recipientPhone,
        String address,
        String deliveryRequest
    ) { }
}
