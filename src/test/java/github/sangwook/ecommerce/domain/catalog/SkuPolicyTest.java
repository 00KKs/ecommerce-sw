package github.sangwook.ecommerce.domain.catalog;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import github.sangwook.ecommerce.catalog.domain.Sku;
import github.sangwook.ecommerce.catalog.policy.SkuCreatePolicy;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class SkuPolicyTest {

    private final AtomicLong skuIdSequence = new AtomicLong(1);

    private Sku createSku(String optionName) {
        Sku sku = new Sku(1L, optionName, 10000);
        ReflectionTestUtils.setField(sku, "id", skuIdSequence.getAndIncrement());
        return sku;
    }

    @Nested
    class SKU_생성_정책 {

        @Test
        void 같은_옵션명이_없으면_통과() {
            List<Sku> skus = List.of(createSku("블랙-M"), createSku("블랙-L"));

            SkuCreatePolicy policy = new SkuCreatePolicy(skus);

            assertDoesNotThrow(() -> policy.validateOptionNameExists("화이트-M"));
        }

        @Test
        void 같은_옵션명이_이미_있으면_예외() {
            List<Sku> skus = List.of(createSku("블랙-M"), createSku("블랙-L"));

            SkuCreatePolicy policy = new SkuCreatePolicy(skus);

            assertThatThrownBy(() -> policy.validateOptionNameExists("블랙-M")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void SKU가_비어있으면_어떤_옵션명이든_통과() {
            SkuCreatePolicy policy = new SkuCreatePolicy(List.of());

            assertDoesNotThrow(() -> policy.validateOptionNameExists("블랙-M"));
        }
    }
}