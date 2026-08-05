package github.sangwook.ecommerce.catalog.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "sku")
@Getter
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "option_name")
    private String optionName;

    @Column(name = "price")
    private int price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SkuSaleStatus status;

    protected Sku() {
    }

    public Sku(Long productId, String optionName, int price) {
        validatePrice(price);
        this.productId = productId;
        this.optionName = optionName;
        this.price = price;
        this.status = SkuSaleStatus.STOPPED;
    }

    public boolean isSellable() {
        return status == SkuSaleStatus.SELLING;
    }

    public void updatePrice(Integer price) {
        validatePrice(price);
        this.price = price;
    }

    private void validatePrice(Integer price) {
        if (price <= 0) throw new IllegalStateException("가격은 0이거나 음수일 수 없습니다.");
    }

    public void changeStatus(SkuSaleStatus status) {
        this.status = status;
    }
}
