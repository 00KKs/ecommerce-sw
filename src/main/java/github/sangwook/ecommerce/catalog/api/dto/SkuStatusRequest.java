package github.sangwook.ecommerce.catalog.api.dto;

import github.sangwook.ecommerce.catalog.domain.SkuSaleStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkuStatusRequest {

    private SkuSaleStatus status;
}
