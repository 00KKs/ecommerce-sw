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
        validateQuantityPositive(quantity);
        stockRepository.increase(skuId, quantity);
        Stock stock = stockRepository.getStockOrThrow(skuId);
        return new StockInboundResponse(stock.getSkuId(), stock.getQuantity());
    }

    public StockResponse getItem(Long skuId) {
        Stock stock = stockRepository.getStockOrThrow(skuId);
        return new StockResponse(stock.getSkuId(), stock.getQuantity());
    }

    @Transactional
    public void decreaseIfEnough(Long skuId, Integer quantity) {
        validateQuantityPositive(quantity);
        int updated = stockRepository.decreaseIfEnough(skuId, quantity);
        if (updated == 0) throw new OutOfStockException(skuId);
    }

    private void validateQuantityPositive(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalStateException("재고는 1 이상의 값만 입력할 수 있습니다.");
        }
    }
}
