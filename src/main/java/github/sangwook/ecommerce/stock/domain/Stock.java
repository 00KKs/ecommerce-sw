package github.sangwook.ecommerce.stock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "stock")
@Getter
public class Stock {

    @Id
    private Long skuId;

    @Column(name = "quantity")
    private Integer quantity;

    protected Stock() {
    }

    public Stock(Long skuId, Integer quantity) {
        this.skuId = skuId;
        this.quantity = quantity;
    }
}
