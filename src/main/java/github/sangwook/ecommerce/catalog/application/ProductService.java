package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.api.dto.ProductSummaryResponse;
import github.sangwook.ecommerce.catalog.domain.Product;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional
    public void create(Long categoryId, String name, String description) {
        categoryService.validateLeafForProduct(categoryId);
        productRepository.save(new Product(categoryId, name, description));
    }

    public List<ProductSummaryResponse> getProductsByCategoryId(Long categoryId) {
        return productRepository.findAllByCategoryId(categoryId)
            .stream()
            .map(p -> new ProductSummaryResponse(p.getId(), p.getName()))
            .toList();
    }
}
