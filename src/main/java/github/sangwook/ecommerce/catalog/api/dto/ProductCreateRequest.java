package github.sangwook.ecommerce.catalog.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateRequest {

    private Long categoryId;
    private String name;
    private String description;

}
