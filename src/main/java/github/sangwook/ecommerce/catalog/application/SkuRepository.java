package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.domain.Sku;

import java.util.List;

public interface SkuRepository {
    List<Sku> findAllByProductId(Long productId);
}
