package github.sangwook.ecommerce.catalog.api;

import github.sangwook.ecommerce.catalog.api.dto.*;
import github.sangwook.ecommerce.catalog.application.ProductService;
import github.sangwook.ecommerce.openapi.ProductOpenApiDocs;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController implements ProductOpenApiDocs {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ProductCreateRequest request) {
        productService.create(request.getCategoryId(), request.getName(), request.getDescription());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductSummaryResponse>> getProductsByCategoryId(@RequestParam Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategoryId(categoryId));
    }

    @PostMapping("/{productId}/update")
    public ResponseEntity<ProductUpdateResponse> update(@PathVariable Long productId, @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.update(productId, request.getName(), request.getDescription()));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getDetail(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductDetail(productId));
    }

    @PostMapping("/{productId}/status")
    public ResponseEntity<ProductStatusResponse> changeStatus(@PathVariable Long productId, @RequestBody ProductStatusRequest request) {
        return ResponseEntity.ok(productService.changeStatus(productId, request.getStatus()));
    }

}
