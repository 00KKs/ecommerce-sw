package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.api.dto.ProductSummaryResponse;
import github.sangwook.ecommerce.catalog.domain.Product;
import github.sangwook.ecommerce.catalog.infrastructure.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void create(Long categoryId, String name, String description) {
        if (!isLeaf(categoryId)) throw new IllegalStateException("상품은 최하위 카테고리에만 추가할 수 있습니다.");
        productRepository.save(new Product(categoryId, name, description));
    }

    public List<ProductSummaryResponse> getProductsByCategoryId(Long categoryId) {
        return productRepository.findAllByCategoryId(categoryId)
            .stream()
            .map(p -> new ProductSummaryResponse(p.getId(), p.getName()))
            .toList();
    }

    public boolean isLeaf(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) throw new IllegalStateException("존재하지 않는 카테고리입니다.");
        return categoryRepository.countDescendants(categoryId) == 0;
    }
}
