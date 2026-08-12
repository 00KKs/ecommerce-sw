package github.sangwook.ecommerce.stock.api;

import github.sangwook.ecommerce.stock.api.dto.StockInboundRequest;
import github.sangwook.ecommerce.stock.api.dto.StockInboundResponse;
import github.sangwook.ecommerce.stock.api.dto.StockResponse;
import github.sangwook.ecommerce.stock.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/api/skus/{skuId}/stock/inbound")
    public ResponseEntity<StockInboundResponse> inbound(@PathVariable Long skuId, @RequestBody StockInboundRequest request) {
        return ResponseEntity.ok(stockService.inbound(skuId, request.getQuantity()));
    }

    @GetMapping("/api/skus/{skuId}/stock")
    public ResponseEntity<StockResponse> getItem(@PathVariable Long skuId) {
        return ResponseEntity.ok(stockService.getItem(skuId));
    }

}
