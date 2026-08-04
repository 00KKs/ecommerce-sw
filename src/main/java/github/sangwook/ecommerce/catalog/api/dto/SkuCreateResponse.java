package github.sangwook.ecommerce.catalog.api.dto;

import lombok.Getter;

@Getter
public class SkuCreateResponse {

    private final Long id;

    public SkuCreateResponse(Long id) {
        this.id = id;
    }
}
