package github.sangwook.ecommerce.stock.application;

import github.sangwook.ecommerce.stock.domain.Stock;

public interface StockRepository {

    Stock save(Stock stock);

    Stock findStockWithWriteLock(Long skuId);

    Stock getStockOrThrow(Long skuId);
}
