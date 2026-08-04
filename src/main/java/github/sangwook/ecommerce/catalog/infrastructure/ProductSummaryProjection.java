package github.sangwook.ecommerce.catalog.infrastructure;

public interface ProductSummaryProjection {
    Long getId();
    String getName();
    Integer getLowestPrice();
}
