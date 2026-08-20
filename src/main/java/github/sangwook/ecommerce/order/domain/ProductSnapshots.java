package github.sangwook.ecommerce.order.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductSnapshots {
    private final List<ProductSnapshot> items;

    public ProductSnapshots(List<ProductSnapshot> items) {
        this.items = items;
    }

    @Getter
    public static class ProductSnapshot {
        private final String productName;
        private final String optionName;
        private final Integer unitPrice;
        private final Integer quantity;

        public ProductSnapshot(String productName, String optionName, Integer unitPrice, Integer quantity) {
            this.productName = productName;
            this.optionName = optionName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }
    }
}
