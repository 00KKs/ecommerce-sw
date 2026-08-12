package github.sangwook.ecommerce.stock.application;

import github.sangwook.ecommerce.stock.api.dto.StockInboundResponse;
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
        if (stock == null) throw new IllegalStateException("skuId=" + skuId + " 에 해당하는 Stock이 초기화되지 않았습니다."); //FIXME 불변식 위반
        stock.inbound(quantity);
        stock = stockRepository.save(stock);
        return new StockInboundResponse(stock.getSkuId(), stock.getQuantity());
    }
}
