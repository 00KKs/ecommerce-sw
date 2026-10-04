package github.sangwook.ecommerce.order.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final Long skuId;
    private final int requestQuantity;
    private final int availableQuantity;

    public InsufficientStockException(Long skuId, int requestQuantity, int availableQuantity) {
        super("재고 부족 - skuId=" + skuId);
        this.skuId = skuId;
        this.requestQuantity = requestQuantity;
        this.availableQuantity = availableQuantity;
    }
}
