package github.sangwook.ecommerce.stock.adapter;

import github.sangwook.ecommerce.catalog.port.StockInitializer;
import github.sangwook.ecommerce.order.port.StockPort;
import github.sangwook.ecommerce.stock.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class StockAdapter implements StockInitializer, StockPort {

    private final StockService stockService;

    @Override
    public void initializeZero(Long skuId) {
        stockService.initializeZero(skuId);
    }

    @Override
    public void deduct(Long skuId, Integer quantity) {
        stockService.decreaseIfEnough(skuId, quantity);
    }
}
