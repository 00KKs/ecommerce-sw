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
        validateQuantityPositive(quantity);
        this.skuId = skuId;
        this.quantity = quantity;
    }

    public void inbound(Integer quantity) {
        validateQuantityPositive(quantity);
        this.quantity += quantity;
    }

    private void validateQuantityPositive(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalStateException("재고는 1 이상의 값만 입력할 수 있습니다.");
        }
    }
}
