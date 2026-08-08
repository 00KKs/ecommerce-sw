package github.sangwook.ecommerce.catalog.infrastructure;

import github.sangwook.ecommerce.catalog.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Query(value = """
    SELECT p.id, p.name, MIN(s.price) as lowestPrice 
    FROM Product p 
    JOIN category_closure cc ON p.category_id = cc.descendant 
    JOIN Sku s on p.id = s.product_id 
    WHERE cc.ancestor = :categoryId 
    AND p.sale_status = 'SELLING' 
    AND s.status = 'SELLING' 
    GROUP BY p.id, p.name
    """, nativeQuery = true)
    List<ProductSummaryProjection> findSellableSummaries(@Param("categoryId") Long categoryId);
}
