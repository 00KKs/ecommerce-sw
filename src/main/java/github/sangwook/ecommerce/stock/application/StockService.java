package github.sangwook.ecommerce.stock.application;

import github.sangwook.ecommerce.stock.OutOfStockException;
import github.sangwook.ecommerce.stock.api.dto.StockInboundResponse;
import github.sangwook.ecommerce.stock.api.dto.StockResponse;
import github.sangwook.ecommerce.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public void initializeZero(Long skuId) {
        stockRepository.save(new Stock(skuId, 0));
    }

    @Transactional
    public StockInboundResponse inbound(Long skuId, Integer quantity) {
        Stock stock = stockRepository.findStockWithWriteLock(skuId);
        stock.inbound(quantity);
        stock = stockRepository.save(stock);
        return new StockInboundResponse(stock.getSkuId(), stock.getQuantity());
    }

    public StockResponse getItem(Long skuId) {
        Stock stock = stockRepository.getStockOrThrow(skuId);
        return new StockResponse(stock.getSkuId(), stock.getQuantity());
    }

    @Transactional
    public void decreaseIfEnough(Long skuId, Integer quantity) {
        int updated = stockRepository.decreaseIfEnough(skuId, quantity);
        if (updated == 0) throw new OutOfStockException(skuId);
    }
}
