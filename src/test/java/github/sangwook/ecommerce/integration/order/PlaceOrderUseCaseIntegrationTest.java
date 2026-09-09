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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
class PlaceOrderUseCaseIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PlaceOrderUseCase placeOrderUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    class 재고_부족_주문_실패_테스트 {
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

    @Nested
    class 재고_복원_확인_테스트 {

        @Nested
        @TestPropertySource(properties = {
            "fake.payment.initiate=ALWAYS_FAIL",
            "fake.payment.confirm=SUCCESS"
        })
        class 결제_요청_실패 {

            @Test
            @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
            @DisplayName("PG사 결제 요청 실패 시 재고를 복원한다.")
            void restoreStock() {
                Long memberId = 1L;
                Long addressId = 1L;
                Map<Long, Integer> orderItems = Map.of(100L, 2);

                PlaceOrderResponse response = placeOrderUseCase.placeOrder(memberId, addressId, orderItems);

                assertThat(response.status()).isEqualTo(OrderDisplayStatus.FAILED);

                Integer remaining = jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE sku_id = 100", Integer.class);
                assertThat(remaining).isEqualTo(10);
            }
        }

        @Nested
        @TestPropertySource(properties = {
            "fake.payment.initiate=SUCCESS",
            "fake.payment.confirm=ALWAYS_FAIL"
        })
        class 결제_승인_실패 {

            @Test
            @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
            @DisplayName("PG사 결제 승인 실패 시 재고를 복원한다.")
            void restoreStock() {
                Long memberId = 1L;
                Long addressId = 1L;
                Map<Long, Integer> orderItems = Map.of(100L, 2);

                PlaceOrderResponse response = placeOrderUseCase.placeOrder(memberId, addressId, orderItems);

                assertThat(response.status()).isEqualTo(OrderDisplayStatus.FAILED);

                Integer remaining = jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE sku_id = 100", Integer.class);
                assertThat(remaining).isEqualTo(10);
            }
        }

    }

    @Nested
    class 결제_기록_확인_테스트 {

        @Nested
        @TestPropertySource(properties = {
            "fake.payment.initiate=ALWAYS_FAIL",
            "fake.payment.confirm=SUCCESS"
        })
        class 결제_요청_실패 {

            @Test
            @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
            @DisplayName("PG사 결제 요청 실패 시 Payment를 저장하지 않는다.")
            void doesNotSavePayment() {
                Long memberId = 1L;
                Long addressId = 1L;
                Map<Long, Integer> orderItems = Map.of(100L, 2);

                PlaceOrderResponse response = placeOrderUseCase.placeOrder(memberId, addressId, orderItems);

                assertThat(response.status()).isEqualTo(OrderDisplayStatus.FAILED);

                Integer paymentCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM payment WHERE order_id = ?",
                    Integer.class,
                    response.orderId());
                assertThat(paymentCount).isEqualTo(0);
            }
        }

        @Nested
        @TestPropertySource(properties = {
            "fake.payment.initiate=SUCCESS",
            "fake.payment.confirm=ALWAYS_FAIL"
        })
        class 결제_승인_실패 {

            @Test
            @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
            @DisplayName("PG사 결제 승인 실패 시 Payment를 ABORTED 상태로 저장한다.")
            void savesFailedPayment() {
                Long memberId = 1L;
                Long addressId = 1L;
                Map<Long, Integer> orderItems = Map.of(100L, 2);

                PlaceOrderResponse response = placeOrderUseCase.placeOrder(memberId, addressId, orderItems);

                assertThat(response.status()).isEqualTo(OrderDisplayStatus.FAILED);

                Map<String, Object> payment = jdbcTemplate.queryForMap("SELECT * FROM payment WHERE order_id = ?", response.orderId());
                assertThat(payment.get("payment_status")).isEqualTo("ABORTED");
            }
        }
    }

    @Nested
    class 재고_보류_확인_테스트 {

        @Nested
        @TestPropertySource(properties = "fake.payment.initiate=RETRYABLE_FAILURE")
        class 결제_요청_일시적_실패 {

            @Test
            @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
            @DisplayName("재시도 가능한 요청 실패 시 재고는 복원되지 않고 계속 보류된다")
            void holdStock() {
                Long memberId = 1L;
                Long addressId = 1L;
                Map<Long, Integer> orderItems = Map.of(100L, 2);

                PlaceOrderResponse response = placeOrderUseCase.placeOrder(memberId, addressId, orderItems);

                assertThat(response.paymentKey()).isNull();
                assertThat(response.status()).isEqualTo(OrderDisplayStatus.PAYMENT_PENDING);

                Integer remaining = jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE sku_id = 100", Integer.class);
                assertThat(remaining).isEqualTo(8);
            }
        }

        @Nested
        @TestPropertySource(properties = {
            "fake.payment.initiate=SUCCESS",
            "fake.payment.confirm=RETRYABLE_FAILURE"
        })
        class 결제_승인_일시적_실패 {

            @Test
            @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
            @DisplayName("재시도 가능한 승인 실패 시 재고는 복원되지 않고 계속 보류된다")
            void holdStock() {
                Long memberId = 1L;
                Long addressId = 1L;
                Map<Long, Integer> orderItems = Map.of(100L, 2);

                PlaceOrderResponse response = placeOrderUseCase.placeOrder(memberId, addressId, orderItems);

                assertThat(response.paymentKey()).isNotNull();
                assertThat(response.status()).isEqualTo(OrderDisplayStatus.PAYMENT_PENDING);

                Integer remaining = jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE sku_id = 100", Integer.class);
                assertThat(remaining).isEqualTo(8);
            }
        }
    }


}