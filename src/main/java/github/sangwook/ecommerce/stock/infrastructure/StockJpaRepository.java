package github.sangwook.ecommerce.stock.infrastructure;

import github.sangwook.ecommerce.stock.domain.Stock;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("select s from Stock s where s.skuId = :skuId")
    Optional<Stock> findStockWithWriteLock(@Param("skuId") Long skuId);

    @Modifying
    @Query("update Stock s set s.quantity = s.quantity - :quantity where s.skuId = :skuId and s.quantity >= :quantity")
    int decreaseIfEnough(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
