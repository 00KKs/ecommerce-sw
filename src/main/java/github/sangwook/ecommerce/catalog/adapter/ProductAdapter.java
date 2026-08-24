package github.sangwook.ecommerce.catalog.adapter;

import github.sangwook.ecommerce.catalog.adapter.dto.ProductInfo;
import github.sangwook.ecommerce.catalog.application.ProductService;
import github.sangwook.ecommerce.order.domain.ProductSnapshots;
import github.sangwook.ecommerce.order.port.ProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class ProductAdapter implements ProductPort {
    private final ProductService productService;

    @Override
    public ProductSnapshots getProductSnapshots(Map<Long, Integer> skuIdQuantityMap) {
        List<ProductSnapshots.ProductSnapshot> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : skuIdQuantityMap.entrySet()) {
            ProductInfo info = productService.getProductInfo(entry.getKey());
            items.add(new ProductSnapshots.ProductSnapshot(info.getProductName(), info.getOptionName(), info.getUnitPrice(), entry.getValue()));
        }
        return new ProductSnapshots(items);
    }
}
