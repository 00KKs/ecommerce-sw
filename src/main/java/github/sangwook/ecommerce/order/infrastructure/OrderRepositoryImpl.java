package github.sangwook.ecommerce.order.infrastructure;

import github.sangwook.ecommerce.order.application.OrderRepository;
import github.sangwook.ecommerce.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }
}
