package github.sangwook.ecommerce.stock.api.dto;

import lombok.Getter;

@Getter
public class StockInboundResponse {

    private final Long skuId;
    private final Integer quantity;

    public StockInboundResponse(Long skuId, Integer quantity) {
        this.skuId = skuId;
        this.quantity = quantity;
    }
}
