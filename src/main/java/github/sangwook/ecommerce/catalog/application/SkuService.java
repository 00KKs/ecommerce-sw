package github.sangwook.ecommerce.catalog.application;

import github.sangwook.ecommerce.catalog.api.dto.SkuCreateResponse;
import github.sangwook.ecommerce.catalog.api.dto.SkuDetailResponse;
import github.sangwook.ecommerce.catalog.api.dto.SkuUpdatePriceResponse;
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

    public List<SkuDetailResponse> getAllByProductId(Long productId) {
        return skuRepository.findAllByProductId(productId)
            .stream()
            .map(sku -> new SkuDetailResponse(sku.getId(), sku.getOptionName(), sku.getPrice(), sku.getStatus()))
            .toList();
    }

    public SkuUpdatePriceResponse updatePrice(Long skuId, Integer price) {
        Sku sku = getById(skuId);
        sku.updatePrice(price);
        sku = skuRepository.save(sku);
        return new SkuUpdatePriceResponse(sku.getId(), sku.getPrice());
    }

    private Sku getById(Long id) {
        return skuRepository.findById(id).orElseThrow(() -> new IllegalStateException("존재하지 않는 SKU입니다."));
    }
}
