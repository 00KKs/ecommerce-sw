package github.sangwook.ecommerce.order.infrastructure;

import github.sangwook.ecommerce.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

}
