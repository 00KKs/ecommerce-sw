package github.sangwook.ecommerce.catalog.api.dto;

import lombok.Getter;

@Getter
public class ProductUpdateResponse {

    private Long id;
    private String name;
    private String description;

    public ProductUpdateResponse(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
