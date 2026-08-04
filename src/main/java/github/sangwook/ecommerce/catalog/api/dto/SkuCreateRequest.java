package github.sangwook.ecommerce.catalog.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkuCreateRequest {

    private String optionName;
    private Integer price;
}
