package github.sangwook.ecommerce.stock.infrastructure;

import github.sangwook.ecommerce.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {
}
