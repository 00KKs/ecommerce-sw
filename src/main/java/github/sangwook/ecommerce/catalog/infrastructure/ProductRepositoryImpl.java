package github.sangwook.ecommerce.catalog.infrastructure;

import github.sangwook.ecommerce.catalog.application.ProductRepository;
import github.sangwook.ecommerce.catalog.domain.Product;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public List<Product> findAllByCategoryId(Long categoryId) {
        return productJpaRepository.findAllByCategoryId(categoryId);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(Long productId) {
        return productJpaRepository.existsById(productId);
    }
}
