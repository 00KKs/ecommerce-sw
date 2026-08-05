package github.sangwook.ecommerce.catalog.api.dto;

import github.sangwook.ecommerce.catalog.domain.SkuSaleStatus;
import lombok.Getter;

@Getter
public class SkuStatusResponse {

    private final Long id;
    private final SkuSaleStatus status;

    public SkuStatusResponse(Long id, SkuSaleStatus status) {
        this.id = id;
        this.status = status;
    }
}
