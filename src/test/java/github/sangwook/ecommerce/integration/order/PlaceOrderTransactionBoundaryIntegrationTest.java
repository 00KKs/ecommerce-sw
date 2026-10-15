package github.sangwook.ecommerce.integration.order;

import github.sangwook.ecommerce.integration.AbstractIntegrationTest;
import github.sangwook.ecommerce.integration.payment.ComposableFakePaymentGateway;
import github.sangwook.ecommerce.order.api.dto.OrderDisplayStatus;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.application.OrderService;
import github.sangwook.ecommerce.order.application.PlaceOrderUseCase;
import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.application.PaymentLookupScheduler;
import github.sangwook.ecommerce.payment.application.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.IllegalTransactionStateException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
public class PlaceOrderTransactionBoundaryIntegrationTest extends AbstractIntegrationTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long ADDRESS_ID = 1L;
    private static final Map<Long, Integer> ORDER_ITEMS = Map.of(100L, 2);
    private static final int INITIAL_STOCK = 10;
    private static final int DEDUCTED_STOCK = 8;

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private PaymentLookupScheduler paymentLookupScheduler;

    @Autowired
    PlaceOrderUseCase placeOrderUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaymentPort paymentPort;

    @MockitoSpyBean
    private PaymentService paymentService;

    @MockitoSpyBean
    private OrderService orderService;

    @Nested
    class 주문_생성_트랜잭션_원자성 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("결제 기록 저장에 실패하면 트랜잭션 전체가 롤백되어 주문·주문상품·결제 기록이 남지 않고 재고도 차감되지 않는다")
        void paymentReadyFails_rollsBackAll() {
            doThrow(dbFailure()).when(paymentService).ready(anyLong(), anyInt(), any());

            assertThatThrownBy(() -> placeOrderUseCase.placeOrder(MEMBER_ID, ADDRESS_ID, ORDER_ITEMS))
                    .isInstanceOf(DataAccessResourceFailureException.class);

            assertThat(count("SELECT COUNT(*) FROM orders")).isZero();
            assertThat(count("SELECT COUNT(*) FROM order_item")).isZero();
            assertThat(count("SELECT COUNT(*) FROM payment")).isZero();
            assertThat(remainingStock()).isEqualTo(INITIAL_STOCK);
        }

        @Test
        @DisplayName("결제 준비는 호출자의 트랜잭션 안에서만 호출할 수 있다")
        void prepareRequiresTransaction() {
            assertThatThrownBy(() -> paymentPort.prepare(1L, 10_000))
                    .isInstanceOf(IllegalTransactionStateException.class);
        }
    }

    @Nested
    class 주문_생성_이후_단계_실패_보상 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("paymentKey 저장에 실패하면 승인 요청 전이므로, 주문을 실패 처리하고 재고를 복원하고 결제를 ABORTED로 닫는다")
        void paymentKeySaveFails() {
            doThrow(dbFailure()).when(paymentService).updatePaymentKey(anyLong(), anyLong(), anyInt(), anyString());
            PlaceOrderResponse response = placeOrderUseCase.placeOrder(MEMBER_ID, ADDRESS_ID, ORDER_ITEMS);

            Long orderId = response.orderId();
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_FAILED");
            assertThat(remainingStock()).isEqualTo(INITIAL_STOCK);
            assertThat(paymentStatus(orderId)).isEqualTo("ABORTED");
            assertThat(fakeGateway().storedPayment(fakePaymentKey(orderId)).getConfirmIdempotencyKeys())
                    .as("PG confirm이 호출되지 않아야 한다")
                    .isEmpty();
        }

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("PG 승인 성공 후 결제 상태 저장에 실패하면, 재고를 복원하지 않고 결제를 UNKNOWN으로 남겨 재조회를 예약한다")
        void approvedButPaymentSaveFails() {
            doThrow(dbFailure()).when(paymentService).success(anyLong());

            PlaceOrderResponse response = placeOrderUseCase.placeOrder(MEMBER_ID, ADDRESS_ID, ORDER_ITEMS);

            Long orderId = response.orderId();
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_PENDING");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
            assertThat(paymentStatus(orderId)).isEqualTo("UNKNOWN");
            assertThat(isNextCheckScheduled(orderId)).isTrue();
        }

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("PG 승인 성공 후 결제 상태 저장에 실패해도, 이후 조회 스케줄러가 결제와 주문을 확정한다")
        void approvedButPaymentSaveFails_recoveredByScheduler() {
            doThrow(dbFailure()).doCallRealMethod().when(paymentService).success(anyLong());
            PlaceOrderResponse response = placeOrderUseCase.placeOrder(MEMBER_ID, ADDRESS_ID, ORDER_ITEMS);

            Long orderId = response.orderId();

            runSchedulerNow(orderId); // fake lookup 기본값: DONE

            assertThat(paymentStatus(orderId)).isEqualTo("DONE");
            assertThat(orderStatus(orderId)).isEqualTo("CONFIRMED");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("결제는 DONE인데 주문 확정 반영에 실패하면, 결제와 어긋나게 주문을 실패 처리하거나 재고를 복원하지 않는다")
        void approvedButOrderConfirmFails() {
            doThrow(dbFailure()).when(orderService).applyPaymentOutcome(any(), anyLong());

            PlaceOrderResponse response = placeOrderUseCase.placeOrder(MEMBER_ID, ADDRESS_ID, ORDER_ITEMS);

            Long orderId = response.orderId();
            assertThat(paymentStatus(orderId)).isEqualTo("DONE");
            assertThat(orderStatus(orderId)).isNotEqualTo("PAYMENT_FAILED");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.initiate=SUCCESS",
            "fake.payment.confirm=ALWAYS_FAIL"
    })
    class 결제_실패_확정_후_주문_반영_실패 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("결제는 ABORTED인데 주문 실패 반영에 실패하면 확인 중으로 응답하고, 주문과 재고는 대사 작업 전까지 보류된다")
        void abortedButOrderFailFails() {
            doThrow(dbFailure()).when(orderService).applyPaymentOutcome(any(), anyLong());

            PlaceOrderResponse response = placeOrderUseCase.placeOrder(MEMBER_ID, ADDRESS_ID, ORDER_ITEMS);

            assertThat(response.status()).isEqualTo(OrderDisplayStatus.PENDING_CONFIRMATION);
            assertThat(paymentStatus(response.orderId())).isEqualTo("ABORTED");
            assertThat(orderStatus(response.orderId())).isEqualTo("PAYMENT_PENDING");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }
    }

    private static DataAccessResourceFailureException dbFailure() {
        return new DataAccessResourceFailureException("DB 연결 실패 (테스트 주입)");
    }

    private static String fakePaymentKey(Long orderId) {
        return "fake-payment-key-" + orderId; // InitiateScenario.SUCCESS의 키 생성 규칙
    }

    private ComposableFakePaymentGateway fakeGateway() {
        return (ComposableFakePaymentGateway) paymentGateway;
    }

    private String orderStatus(Long orderId) {
        return jdbcTemplate.queryForObject("SELECT status FROM orders WHERE id = ?", String.class, orderId);
    }

    private String paymentStatus(Long orderId) {
        return jdbcTemplate.queryForObject("SELECT payment_status FROM payment WHERE order_id = ?", String.class, orderId);
    }

    private boolean isNextCheckScheduled(Long orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT next_check_at > now() FROM payment WHERE order_id = ?", Boolean.class, orderId);
    }

    private int remainingStock() {
        return jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE sku_id = 100", Integer.class);
    }

    // backoff 대기 없이 바로 조회 대상이 되도록 next_check_at을 과거로 당긴 뒤 스케줄러를 실행한다.
    private void runSchedulerNow(Long orderId) {
        jdbcTemplate.update("UPDATE payment SET next_check_at = now() - interval '1 second' WHERE order_id = ?", orderId);
        paymentLookupScheduler.lookupUnknownPayments();
    }

    private int count(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
