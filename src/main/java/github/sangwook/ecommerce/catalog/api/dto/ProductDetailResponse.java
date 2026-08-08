package github.sangwook.ecommerce.catalog.api.dto;

import github.sangwook.ecommerce.catalog.domain.ProductSaleStatus;
import java.util.List;
import lombok.Getter;

@Getter
public class ProductDetailResponse {

    private final Long id;
    private final Long categoryId;
    private final String name;
    private final String description;
    private final ProductSaleStatus status;
    private final List<SkuDetailResponse> skus;

    public ProductDetailResponse(Long id, Long categoryId, String name, String description, ProductSaleStatus status, List<SkuDetailResponse> skus) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.skus = skus;
    }
}

