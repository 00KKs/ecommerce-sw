package github.sangwook.ecommerce.catalog.api;

import github.sangwook.ecommerce.catalog.api.dto.*;
import github.sangwook.ecommerce.catalog.application.SkuService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @PostMapping("/api/products/{productId}/skus")
    public ResponseEntity<SkuCreateResponse> create(@PathVariable Long productId, @RequestBody SkuCreateRequest request) {
        return ResponseEntity.ok(skuService.create(productId, request.getOptionName(), request.getPrice()));
    }

    @GetMapping("/api/products/{productId}/skus")
    public ResponseEntity<List<SkuDetailResponse>> getList(@PathVariable Long productId) {
        return ResponseEntity.ok(skuService.getAllByProductId(productId));
    }

    @PostMapping("/api/skus/{skuId}/price")
    public ResponseEntity<SkuUpdatePriceResponse> updatePrice(@PathVariable Long skuId, @RequestBody SkuUpdatePriceRequest request) {
        return ResponseEntity.ok(skuService.updatePrice(skuId, request.getPrice()));
    }

    @PostMapping("/api/skus/{skuId}/status")
    public ResponseEntity<SkuStatusResponse> changeStatus(@PathVariable Long skuId, @RequestBody SkuStatusRequest request) {
        return ResponseEntity.ok(skuService.changeStatus(skuId, request.getStatus()));
    }
}
