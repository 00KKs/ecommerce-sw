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

    @Column(name = "product_name")
    private String productName;

    @Column(name = "option_name")
    private String optionName;

    @Column(name = "unit_price")
    private Integer unitPrice;

    @Column(name = "quantity")
    private Integer quantity;

    protected OrderItem() {
    }

    public OrderItem(String productName, String optionName, Integer unitPrice, Integer quantity) {
        this.productName = productName;
        this.optionName = optionName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
}
