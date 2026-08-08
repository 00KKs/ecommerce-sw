package github.sangwook.ecommerce.catalog.api.dto;

import lombok.Getter;

@Getter
public class SkuUpdatePriceResponse {

    private final Long id;
    private final Integer price;

    public SkuUpdatePriceResponse(Long id, Integer price) {
        this.id = id;
        this.price = price;
    }
}
