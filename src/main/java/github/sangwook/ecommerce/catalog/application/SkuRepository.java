package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.domain.Sku;

import java.util.List;
import java.util.Optional;

public interface SkuRepository {
    List<Sku> findAllByProductId(Long productId);

    Sku save(Sku sku);

    Optional<Sku> findById(Long id);
}
