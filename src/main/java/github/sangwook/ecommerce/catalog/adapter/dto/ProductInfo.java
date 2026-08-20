package github.sangwook.ecommerce.catalog.adapter.dto;

import lombok.Getter;

@Getter
public class ProductInfo {
    private final String productName;
    private final String optionName;
    private final Integer unitPrice;

    public ProductInfo(String productName, String optionName, Integer unitPrice) {
        this.productName = productName;
        this.optionName = optionName;
        this.unitPrice = unitPrice;
    }
}
