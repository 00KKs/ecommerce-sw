package github.sangwook.ecommerce.order.application;

import github.sangwook.ecommerce.order.domain.Order;

public interface OrderRepository {

    Order save(Order order);
}
