package github.sangwook.ecommerce.catalog.api.dto;

import github.sangwook.ecommerce.catalog.domain.ProductSaleStatus;
import lombok.Getter;

@Getter
public class ProductStatusResponse {

    private final Long id;
    private final ProductSaleStatus status;

    public ProductStatusResponse(Long id, ProductSaleStatus status) {
        this.id = id;
        this.status = status;
    }
}
