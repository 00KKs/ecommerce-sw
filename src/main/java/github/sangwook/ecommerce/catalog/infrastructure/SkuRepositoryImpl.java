package github.sangwook.ecommerce.catalog.infrastructure;

import github.sangwook.ecommerce.catalog.application.SkuRepository;
import github.sangwook.ecommerce.catalog.domain.Sku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SkuRepositoryImpl implements SkuRepository {

    private final SkuJpaRepository skuJpaRepository;

    @Override
    public List<Sku> findAllByProductId(Long productId) {
        return skuJpaRepository.findAllByProductId(productId);
    }

    @Override
    public Sku save(Sku sku) {
        return skuJpaRepository.save(sku);
    }
}
