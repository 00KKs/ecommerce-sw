package github.sangwook.ecommerce.catalog.api.dto;

import github.sangwook.ecommerce.catalog.domain.ProductSaleStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ProductDetailResponse {

    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private ProductSaleStatus status;
    private List<Object> skus; //FIXME sku 구현 후 변경 필요

    public ProductDetailResponse(Long id, Long categoryId, String name, String description, ProductSaleStatus status) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.skus = new ArrayList<>();
    }
}
