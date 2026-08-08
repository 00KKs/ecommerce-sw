package github.sangwook.ecommerce.catalog.api.dto;

import lombok.Getter;

@Getter
public class ProductSummaryResponse {

    private Long id;
    private String name;
    private Integer lowestPrice;

    public ProductSummaryResponse(Long id, String name, Integer lowestPrice) {
        this.id = id;
        this.name = name;
        this.lowestPrice = lowestPrice;
    }
}
