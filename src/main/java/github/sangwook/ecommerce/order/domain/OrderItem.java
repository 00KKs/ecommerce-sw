package github.sangwook.ecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "option_name", nullable = false)
    private String optionName;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    protected OrderItem() {
    }

    public OrderItem(Long skuId, String productName, String optionName, Integer unitPrice, Integer quantity) {
        this.skuId = skuId;
        this.productName = productName;
        this.optionName = optionName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
}
