package github.sangwook.ecommerce.stock.application;

import github.sangwook.ecommerce.stock.domain.Stock;

public interface StockRepository {

    Stock save(Stock stock);

    Stock getStockOrThrow(Long skuId);

    int decreaseIfEnough(Long skuId, Integer quantity);

    void increase(Long skuId, Integer quantity);

    int findQuantityBySkuId(Long skuId);
}
