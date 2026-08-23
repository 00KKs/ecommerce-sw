package github.sangwook.ecommerce.order.application;

import github.sangwook.ecommerce.order.domain.Order;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);
}
