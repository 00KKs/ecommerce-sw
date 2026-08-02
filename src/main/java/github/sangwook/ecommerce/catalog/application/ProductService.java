package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.api.dto.ProductDetailResponse;
import github.sangwook.ecommerce.catalog.api.dto.ProductSummaryResponse;
import github.sangwook.ecommerce.catalog.api.dto.ProductUpdateResponse;
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

    @Transactional
    public ProductUpdateResponse update(Long productId, String name, String description) {
        Product product = getById(productId);
        product.update(name, description);
        product = productRepository.save(product);
        return new ProductUpdateResponse(product.getId(), product.getName(), product.getDescription());
    }

    public List<ProductSummaryResponse> getProductsByCategoryId(Long categoryId) {
        return productRepository.findAllByCategoryId(categoryId)
            .stream()
            .filter(Product::isDisplayable)
            .map(p -> new ProductSummaryResponse(p.getId(), p.getName()))
            .toList();
    }

    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = getById(productId);
        if (!product.isDisplayable()) throw new IllegalStateException("상품을 찾을 수 없습니다.");
        return new ProductDetailResponse(
            product.getId(),
            product.getCategoryId(),
            product.getName(),
            product.getDescription(),
            product.getSaleStatus()
        );
    }

    private Product getById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalStateException("상품을 찾을 수 없습니다."));
    }
}
