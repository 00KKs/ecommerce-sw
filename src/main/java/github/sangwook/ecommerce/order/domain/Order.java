package github.sangwook.ecommerce.order.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Table(name = "orders")
@Getter
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Embedded
    private ShippingAddress shippingAddress;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    protected Order() {
    }

    public Order(OrderStatus status, Integer totalPrice, ShippingAddress shippingAddress) {
        this.status = status;
        this.totalPrice = totalPrice;
        this.shippingAddress = shippingAddress;
    }

    public void addOrderItem(String productName, String optionName, Integer unitPrice, Integer quantity) {
        orderItems.add(new OrderItem(productName, optionName, unitPrice, quantity));
    }
}
