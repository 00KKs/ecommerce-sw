package github.sangwook.ecommerce.stock.api.dto;

import lombok.Getter;

@Getter
public class StockResponse {
    private final Long skuId;
    private final Integer quantity;

    public StockResponse(Long skuId, Integer quantity) {
        this.skuId = skuId;
        this.quantity = quantity;
    }
}
