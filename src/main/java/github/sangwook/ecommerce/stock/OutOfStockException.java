package github.sangwook.ecommerce.stock;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(Long skuId) {
        super("skuId: " + skuId + " 상품의 재고가 부족합니다.");
    }
}
