package github.sangwook.ecommerce.catalog.adapter.dto;

import lombok.Getter;

@Getter
public class ProductInfo {
    private final Long skuId;
    private final String productName;
    private final String optionName;
    private final Integer unitPrice;

    public ProductInfo(Long skuId, String productName, String optionName, Integer unitPrice) {
        this.skuId = skuId;
        this.productName = productName;
        this.optionName = optionName;
        this.unitPrice = unitPrice;
    }
}
