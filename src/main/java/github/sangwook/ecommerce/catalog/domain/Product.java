package github.sangwook.ecommerce.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false)
    private ProductSaleStatus saleStatus;

    protected Product() {
    }

    public Product(Long categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.saleStatus = ProductSaleStatus.DRAFT;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void changeStatus(ProductSaleStatus status) {
        if (status == ProductSaleStatus.DRAFT) throw new IllegalStateException("DRAFT 상태로는 변경할 수 없습니다.");
        this.saleStatus = status;
    }

    public boolean isSellable() {
        return saleStatus == ProductSaleStatus.SELLING;
    }
}
