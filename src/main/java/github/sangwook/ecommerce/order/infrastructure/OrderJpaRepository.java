package github.sangwook.ecommerce.order.infrastructure;

import github.sangwook.ecommerce.order.domain.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o join fetch o.orderItems where o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

}
