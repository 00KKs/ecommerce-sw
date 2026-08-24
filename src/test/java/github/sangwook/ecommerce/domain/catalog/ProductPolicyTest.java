package github.sangwook.ecommerce.domain.catalog;

import github.sangwook.ecommerce.catalog.policy.ProductSalePolicy;
import github.sangwook.ecommerce.catalog.domain.ProductSaleStatus;
import github.sangwook.ecommerce.catalog.domain.Sku;
import github.sangwook.ecommerce.catalog.domain.SkuSaleStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static github.sangwook.ecommerce.catalog.domain.SkuSaleStatus.SELLING;
import static github.sangwook.ecommerce.catalog.domain.SkuSaleStatus.STOPPED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ProductPolicyTest {

    private final AtomicLong skuIdSequence = new AtomicLong(1);

    private Sku createSku(SkuSaleStatus status) {
        Sku sku = new Sku(1L, "옵션-" + skuIdSequence.get(), 10000);
        ReflectionTestUtils.setField(sku, "id", skuIdSequence.getAndIncrement());
        if (status == SELLING) {
            ReflectionTestUtils.setField(sku, "status", SELLING);
        }
        return sku;
    }

    @Nested
    class 판매상태_전환_정책 {

        @Test
        void SELLING_상태인_SKU가_최소_하나_있으면_SELLING_전환_가능() {
            List<Sku> skus = List.of(createSku(SELLING), createSku(STOPPED));

            ProductSalePolicy policy = new ProductSalePolicy(skus);

            assertDoesNotThrow(() -> policy.validateTransitionTo(ProductSaleStatus.SELLING));
        }

        @Test
        void SELLING_상태인_SKU가_하나도_없으면_SELLING_전환_불가() {
            List<Sku> skus = List.of(createSku(STOPPED), createSku(STOPPED));

            ProductSalePolicy policy = new ProductSalePolicy(skus);

            assertThatThrownBy(() -> policy.validateTransitionTo(ProductSaleStatus.SELLING)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void SKU가_비어있으면_SELLING_전환_불가() {
            ProductSalePolicy policy = new ProductSalePolicy(List.of());

            assertThatThrownBy(() -> policy.validateTransitionTo(ProductSaleStatus.SELLING)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void STOPPED_전환은_SKU_상태와_무관하게_가능() {
            List<Sku> skus = List.of(createSku(STOPPED));

            ProductSalePolicy policy = new ProductSalePolicy(skus);

            assertDoesNotThrow(() -> policy.validateTransitionTo(ProductSaleStatus.STOPPED));
        }
    }
}
