package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.domain.Product;
import java.util.List;

public interface ProductRepository {

    List<Product> findAllByCategoryId(Long categoryId);

    Product save(Product product);
}
