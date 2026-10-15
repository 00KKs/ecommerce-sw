package github.sangwook.ecommerce.order.infrastructure;

import github.sangwook.ecommerce.order.application.OrderRepository;
import github.sangwook.ecommerce.order.domain.Order;
import github.sangwook.ecommerce.order.domain.OrderStatus;
import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<Order> findByIdWithItems(Long id) {
        return orderJpaRepository.findByIdWithItems(id);
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus orderStatus) {
        return orderJpaRepository.findAllByStatus(orderStatus);
    }
}
