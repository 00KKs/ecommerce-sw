package github.sangwook.ecommerce.order.port;

public interface StockPort {
    void deduct(Long skuId, Integer quantity);

    void recover(Long skuId, Integer quantity);
}
