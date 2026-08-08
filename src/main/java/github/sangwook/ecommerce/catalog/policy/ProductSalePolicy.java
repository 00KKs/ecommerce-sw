package github.sangwook.ecommerce.catalog.policy;

import github.sangwook.ecommerce.catalog.domain.ProductSaleStatus;
import github.sangwook.ecommerce.catalog.domain.Sku;
import java.util.List;

public class ProductSalePolicy {
    private final List<Sku> skus;

    public ProductSalePolicy(List<Sku> skus) {
        this.skus = skus;
    }

    public void validateTransitionTo(ProductSaleStatus status) {
        if (status == ProductSaleStatus.SELLING && skus.stream().noneMatch(Sku::isSellable)) {
            throw new IllegalStateException("SELLING 상태인 SKU가 최소 하나 있어야 합니다.");
        }
    }

}
