package github.sangwook.ecommerce.order.api.dto;

import github.sangwook.ecommerce.order.application.OrderDisplayStatus;
import lombok.Getter;

@Getter
public class PlaceOrderResponse {

    private Long orderId;
    private OrderDisplayStatus status;
    private Integer totalPrice;
    private String paymentKey;

    public PlaceOrderResponse(Long orderId, OrderDisplayStatus status, Integer totalPrice, String paymentKey) {
        this.orderId = orderId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.paymentKey = paymentKey;
    }
}
