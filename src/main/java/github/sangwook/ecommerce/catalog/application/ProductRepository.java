package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.domain.Product;
import github.sangwook.ecommerce.catalog.infrastructure.ProductSummaryProjection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    boolean existsById(Long productId);

    List<ProductSummaryProjection> findSellableSummaries(Long categoryId);
}
