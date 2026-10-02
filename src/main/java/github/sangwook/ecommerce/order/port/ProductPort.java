package github.sangwook.ecommerce.order.port;

import github.sangwook.ecommerce.order.port.dto.ProductSnapshots;

import java.util.Map;

public interface ProductPort {

    ProductSnapshots getProductSnapshots(Map<Long, Integer> skuIdQuantityMap);
}
