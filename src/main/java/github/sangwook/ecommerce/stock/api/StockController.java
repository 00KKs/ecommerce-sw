package github.sangwook.ecommerce.stock.api;

import github.sangwook.ecommerce.stock.api.dto.StockInboundRequest;
import github.sangwook.ecommerce.stock.api.dto.StockInboundResponse;
import github.sangwook.ecommerce.stock.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/api/skus/{skuId}/stock/inbound")
    public ResponseEntity<StockInboundResponse> inbound(@PathVariable Long skuId, @RequestBody StockInboundRequest request) {
        return ResponseEntity.ok(stockService.inbound(skuId, request.getQuantity()));
    }

}
