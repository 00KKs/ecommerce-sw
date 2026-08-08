package github.sangwook.ecommerce.catalog.policy;

import github.sangwook.ecommerce.catalog.domain.Sku;
import java.util.List;

public class SkuCreatePolicy {
    private final List<Sku> skus;

    public SkuCreatePolicy(List<Sku> skus) {
        this.skus = skus;
    }

    public void validateOptionNameExists(String optionName) {
        skus.stream().filter(s -> s.getOptionName().equals(optionName)).findFirst().ifPresent(s -> {
            throw new IllegalStateException("옵션 이름은 중복될 수 없습니다.");
        });
    }
}
