package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.domain.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findAllByCategoryId(Long categoryId);

    Product save(Product product);

    Optional<Product> findById(Long id);

    boolean existsById(Long productId);
}
