package github.sangwook.ecommerce.order.port;

public interface StockPort {
    void deduct(Long skuId, Integer quantity);
}
