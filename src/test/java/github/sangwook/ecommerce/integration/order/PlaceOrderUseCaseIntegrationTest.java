package github.sangwook.ecommerce.integration.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import github.sangwook.ecommerce.integration.AbstractIntegrationTest;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.application.OrderDisplayStatus;
import github.sangwook.ecommerce.order.application.PlaceOrderUseCase;
import github.sangwook.ecommerce.order.exception.OrderFailedException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
class PlaceOrderUseCaseIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PlaceOrderUseCase placeOrderUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
    @DisplayName("재고가 충분하면 주문이 생성되고 결제 성공 후 확정된다")
    void placeOrder_success() {
        Long memberId = 1L;
        Long addressId = 1L;
        Map<Long, Integer> orderItems = Map.of(100L, 2);

        PlaceOrderResponse response = placeOrderUseCase.placeOrder(memberId, addressId, orderItems);

        assertThat(response.orderId()).isNotNull();
        assertThat(response.status()).isEqualTo(OrderDisplayStatus.CONFIRMED);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalPrice()).isEqualTo(40000);

        Integer remaining = jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE sku_id = 100", Integer.class);
        assertThat(remaining).isEqualTo(8);
    }

    @Test
    @DisplayName("재고가 부족하면 주문 실패 예외가 발생하고 재고가 차감되지 않는다")
    @Sql(scripts = "/test-data/out-of-stock.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
    void placeOrder_outOfStock() {
        Long memberId = 1L;
        Long addressId = 1L;
        Map<Long, Integer> orderItems = Map.of(100L, 5);

        assertThatThrownBy(() -> placeOrderUseCase.placeOrder(memberId, addressId, orderItems)).isInstanceOf(OrderFailedException.class);

        Integer stock = jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE sku_id = 100", Integer.class);
        assertThat(stock).isEqualTo(1);
    }
}