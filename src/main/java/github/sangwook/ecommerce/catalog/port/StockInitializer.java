package github.sangwook.ecommerce.catalog.port;

public interface StockInitializer {
    void initializeZero(Long skuId);
}
