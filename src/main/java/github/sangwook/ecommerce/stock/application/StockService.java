package github.sangwook.ecommerce.stock.application;

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
}
