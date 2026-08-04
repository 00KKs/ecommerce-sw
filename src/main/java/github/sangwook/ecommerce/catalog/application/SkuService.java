package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.api.dto.SkuCreateResponse;
import github.sangwook.ecommerce.catalog.domain.Sku;
import github.sangwook.ecommerce.catalog.policy.SkuCreatePolicy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkuService {

    private final ProductService productService;

    private final SkuRepository skuRepository;

    @Transactional
    public SkuCreateResponse create(Long productId, String optionName, Integer price) {
        productService.validateExists(productId);
        List<Sku> skus = getByProductId(productId);

        SkuCreatePolicy policy = new SkuCreatePolicy(skus);
        policy.validateOptionNameExists(optionName);

        Sku sku = skuRepository.save(new Sku(productId, optionName, price));
        return new SkuCreateResponse(sku.getId());
    }

    public List<Sku> getByProductId(Long productId) {
        return skuRepository.findAllByProductId(productId);
    }
}
