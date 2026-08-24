package github.sangwook.ecommerce.order.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceOrderRequest {
    private Long skuId;
    private Integer quantity;
    private Long addressId;
}
