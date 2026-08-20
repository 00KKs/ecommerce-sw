package github.sangwook.ecommerce.stock.infrastructure;

import github.sangwook.ecommerce.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

    @Modifying
    @Query("update Stock s set s.quantity = s.quantity - :quantity where s.skuId = :skuId and s.quantity >= :quantity")
    int decreaseIfEnough(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("update Stock s set s.quantity = s.quantity + :quantity where s.skuId = :skuId")
    void increase(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
