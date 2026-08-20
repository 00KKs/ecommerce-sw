package github.sangwook.ecommerce.stock.infrastructure;

import github.sangwook.ecommerce.stock.application.StockRepository;
import github.sangwook.ecommerce.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final StockJpaRepository stockJpaRepository;

    @Override
    public Stock save(Stock stock) {
        return stockJpaRepository.save(stock);
    }

    @Override
    public Stock getStockOrThrow(Long skuId) {
        return requireStock(stockJpaRepository.findById(skuId), skuId);
    }

    @Override
    public int decreaseIfEnough(Long skuId, Integer quantity) {
        return stockJpaRepository.decreaseIfEnough(skuId, quantity);
    }

    @Override
    public void increase(Long skuId, Integer quantity) {
        stockJpaRepository.increase(skuId, quantity);
    }

    private Stock requireStock(Optional<Stock> stock, Long skuId) {
        return stock.orElseThrow(() -> new IllegalStateException("skuId=" + skuId + " 에 해당하는 Stock이 초기화되지 않았습니다.")); //FIXME 불변식 위반
    }
}
