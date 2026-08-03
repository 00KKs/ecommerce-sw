package github.sangwook.ecommerce.catalog.infrastructure;

import github.sangwook.ecommerce.catalog.domain.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkuJpaRepository extends JpaRepository<Sku, Long> {
    List<Sku> findAllByProductId(Long productId);
}
