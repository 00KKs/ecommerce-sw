package github.sangwook.ecommerce.catalog.api.dto;

import github.sangwook.ecommerce.catalog.domain.SkuSaleStatus;
import lombok.Getter;

@Getter
public class SkuDetailResponse {

    private final Long id;
    private final String optionName;
    private final Integer price;
    private final SkuSaleStatus status;

    public SkuDetailResponse(Long id, String optionName, Integer price, SkuSaleStatus status) {
        this.id = id;
        this.optionName = optionName;
        this.price = price;
        this.status = status;
    }
}
