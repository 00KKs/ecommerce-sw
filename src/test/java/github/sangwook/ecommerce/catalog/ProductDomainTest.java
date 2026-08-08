package github.sangwook.ecommerce.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import github.sangwook.ecommerce.catalog.domain.Product;
import github.sangwook.ecommerce.catalog.domain.ProductSaleStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProductDomainTest {

    @Nested
    class 상품_등록 {

        @Test
        void 상품은_최초_생성_시_DRAFT_상태다() {
            Product product = new Product(1L, "상품1", "설명1");
            assertThat(product.getSaleStatus()).isEqualTo(ProductSaleStatus.DRAFT);
        }

    }

    @Nested
    class 상품_판매상태_변경 {

        @Test
        void SELLING과_STOPPED는_서로_전환_가능하다() {
            Product product = new Product(1L, "상품1", "설명1");
            product.changeStatus(ProductSaleStatus.SELLING);

            assertDoesNotThrow(() -> product.changeStatus(ProductSaleStatus.STOPPED));
            assertDoesNotThrow(() -> product.changeStatus(ProductSaleStatus.SELLING));
        }

        @Test
        void SELLING_또는_STOPPED_상태에서_DRAFT로는_되돌릴_수_없다 () {
            Product product = new Product(1L, "상품1", "설명1");
            product.changeStatus(ProductSaleStatus.SELLING);

            assertThatThrownBy(() -> product.changeStatus(ProductSaleStatus.DRAFT)).isInstanceOf(IllegalStateException.class);
        }

    }

}
