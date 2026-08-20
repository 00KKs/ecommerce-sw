package github.sangwook.ecommerce.stock;

import lombok.Getter;

@Getter
public class OutOfStockException extends RuntimeException {

    private final Long skuId;
    private final Integer requestQuantity;
    private final Integer availableQuantity;

    public OutOfStockException(Long skuId, Integer requestQuantity, Integer availableQuantity) {
        this.skuId = skuId;
        this.requestQuantity = requestQuantity;
        this.availableQuantity = availableQuantity;
    }
}
