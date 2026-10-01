package github.sangwook.ecommerce.integration.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import github.sangwook.ecommerce.integration.AbstractIntegrationTest;
import github.sangwook.ecommerce.order.api.dto.OrderDisplayStatus;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.application.PlaceOrderUseCase;
import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.application.PaymentLookupScheduler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

/**
 * confirm 타임아웃으로 UNKNOWN이 된 결제를 스케줄러가 재조회한 뒤
 * Payment 상태와 Order 상태(재고 포함)가 어긋나지 않는지 검증한다.
 * 모든 케이스는 confirm=TIMEOUT으로 주문을 만들어 UNKNOWN 결제를 준비한 뒤 시작한다.
 */
@SpringBootTest
class PaymentLookupSchedulerIntegrationTest extends AbstractIntegrationTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long ADDRESS_ID = 1L;
    private static final Map<Long, Integer> ORDER_ITEMS = Map.of(100L, 2);
    private static final int INITIAL_STOCK = 10;
    private static final int DEDUCTED_STOCK = 8;

    @Autowired
    private PlaceOrderUseCase placeOrderUseCase;

    @Autowired
    private PaymentLookupScheduler paymentLookupScheduler;

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.confirm=TIMEOUT",
            "fake.payment.lookup=DONE"
    })
    class 재조회_결과_승인_완료 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("Payment는 DONE, 주문은 CONFIRMED가 되고 재고는 차감된 상태로 유지된다")
        void confirmsOrder() {
            Long orderId = placeUnknownOrder();

            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("DONE");
            assertThat(orderStatus(orderId)).isEqualTo("CONFIRMED");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("이미 처리된 결제는 다음 주기에 다시 조회되지 않는다")
        void doesNotReprocess() {
            Long orderId = placeUnknownOrder();

            runSchedulerNow(orderId);
            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("DONE");
            assertThat(orderStatus(orderId)).isEqualTo("CONFIRMED");
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.confirm=TIMEOUT",
            "fake.payment.lookup=DONE_AMOUNT_MISMATCH"
    })
    class 재조회_결과_금액_불일치 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("Payment는 수동 확인 대상이 되고, 주문은 확정도 실패도 하지 않고 보류된다")
        void requiresManualReview() {
            Long orderId = placeUnknownOrder();

            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("MANUAL_REVIEW_REQUIRED");
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_PENDING");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.confirm=TIMEOUT",
            "fake.payment.lookup=ABORTED"
    })
    class 재조회_결과_승인_실패 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("Payment는 ABORTED, 주문은 PAYMENT_FAILED가 되고 재고가 복원된다")
        void failsOrderAndRestoresStock() {
            Long orderId = placeUnknownOrder();

            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("ABORTED");
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_FAILED");
            assertThat(remainingStock()).isEqualTo(INITIAL_STOCK);
        }

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("스케줄러가 여러 번 돌아도 재고는 한 번만 복원된다")
        void restoresStockOnlyOnce() {
            Long orderId = placeUnknownOrder();

            runSchedulerNow(orderId);
            runSchedulerNow(orderId);

            assertThat(remainingStock()).isEqualTo(INITIAL_STOCK);
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.confirm=TIMEOUT",
            "fake.payment.lookup=NOT_FOUND"
    })
    class 재조회_결과_PG에_내역_없음 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("Payment는 수동 확인 대상이 되고 주문은 보류된다")
        void requiresManualReview() {
            Long orderId = placeUnknownOrder();

            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("MANUAL_REVIEW_REQUIRED");
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_PENDING");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.confirm=TIMEOUT",
            "fake.payment.lookup=UNAVAILABLE"
    })
    class 재조회_자체_실패 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("UNKNOWN을 유지한 채 checkCount가 증가하고 다음 조회가 예약된다")
        void schedulesNextCheck() {
            Long orderId = placeUnknownOrder();
            int checkCountBefore = checkCount(orderId);

            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("UNKNOWN");
            assertThat(checkCount(orderId)).isEqualTo(checkCountBefore + 1);
            Boolean scheduledInFuture = jdbcTemplate.queryForObject(
                    "SELECT next_check_at > now() FROM payment WHERE order_id = ?", Boolean.class, orderId);
            assertThat(scheduledInFuture).isTrue();
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_PENDING");
        }

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("재시도 한도를 넘기면 수동 확인 대상이 되고 주문은 보류된다")
        void exceedsRetryLimit() {
            Long orderId = placeUnknownOrder();

            // 타임아웃 시점에 checkCount=1, 이후 2,3,4 → 4회차 실행에서 MANUAL_REVIEW_REQUIRED
            for (int i = 0; i < 4; i++) {
                runSchedulerNow(orderId);
            }

            assertThat(paymentStatus(orderId)).isEqualTo("MANUAL_REVIEW_REQUIRED");
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_PENDING");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.confirm=TIMEOUT",
            "fake.payment.confirm-retry=SUCCESS",
            "fake.payment.lookup=READY"
    })
    class 재조회_결과_미승인_후_재승인_성공 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("confirm을 다시 호출해 Payment는 DONE, 주문은 CONFIRMED가 된다")
        void confirmsOrder() {
            Long orderId = placeUnknownOrder();

            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("DONE");
            assertThat(orderStatus(orderId)).isEqualTo("CONFIRMED");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("재승인 요청은 최초 confirm과 같은 멱등키를 사용한다")
        void reusesIdempotencyKey() {
            PlaceOrderResponse response = placeOrderUseCase.placeOrder(MEMBER_ID, ADDRESS_ID, ORDER_ITEMS);

            runSchedulerNow(response.orderId());

            List<UUID> idempotencyKeys = fakeGateway().storedPayment(response.paymentKey()).getConfirmIdempotencyKeys();
            assertThat(idempotencyKeys).hasSize(2);
            assertThat(idempotencyKeys.get(1)).isEqualTo(idempotencyKeys.get(0));
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.confirm=TIMEOUT",
            "fake.payment.confirm-retry=ALWAYS_FAIL",
            "fake.payment.lookup=READY"
    })
    class 재조회_결과_미승인_후_재승인_실패 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("Payment는 ABORTED, 주문은 PAYMENT_FAILED가 되고 재고가 복원된다")
        void failsOrderAndRestoresStock() {
            Long orderId = placeUnknownOrder();

            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("ABORTED");
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_FAILED");
            assertThat(remainingStock()).isEqualTo(INITIAL_STOCK);
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "fake.payment.confirm=TIMEOUT",
            "fake.payment.confirm-retry=TIMEOUT",
            "fake.payment.lookup=READY"
    })
    class 재조회_결과_미승인_후_재승인도_타임아웃 {

        @Test
        @Sql(scripts = "/test-data/place-order-success.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "/test-data/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        @DisplayName("UNKNOWN을 유지한 채 다음 조회가 예약되고 주문은 보류된다")
        void staysUnknown() {
            Long orderId = placeUnknownOrder();
            int checkCountBefore = checkCount(orderId);

            runSchedulerNow(orderId);

            assertThat(paymentStatus(orderId)).isEqualTo("UNKNOWN");
            assertThat(checkCount(orderId)).isEqualTo(checkCountBefore + 1);
            assertThat(orderStatus(orderId)).isEqualTo("PAYMENT_PENDING");
            assertThat(remainingStock()).isEqualTo(DEDUCTED_STOCK);
        }
    }

    private Long placeUnknownOrder() {
        PlaceOrderResponse response = placeOrderUseCase.placeOrder(MEMBER_ID, ADDRESS_ID, ORDER_ITEMS);
        assertThat(response.status()).isEqualTo(OrderDisplayStatus.PENDING_CONFIRMATION);
        assertThat(paymentStatus(response.orderId())).isEqualTo("UNKNOWN");
        return response.orderId();
    }

    // backoff 대기 없이 바로 조회 대상이 되도록 next_check_at을 과거로 당긴 뒤 스케줄러를 실행한다.
    private void runSchedulerNow(Long orderId) {
        jdbcTemplate.update("UPDATE payment SET next_check_at = now() - interval '1 second' WHERE order_id = ?", orderId);
        paymentLookupScheduler.lookupUnknownPayments();
    }

    private ComposableFakePaymentGateway fakeGateway() {
        return (ComposableFakePaymentGateway) paymentGateway;
    }

    private String paymentStatus(Long orderId) {
        return jdbcTemplate.queryForObject("SELECT payment_status FROM payment WHERE order_id = ?", String.class, orderId);
    }

    private int checkCount(Long orderId) {
        return jdbcTemplate.queryForObject("SELECT check_count FROM payment WHERE order_id = ?", Integer.class, orderId);
    }

    private String orderStatus(Long orderId) {
        return jdbcTemplate.queryForObject("SELECT status FROM orders WHERE id = ?", String.class, orderId);
    }

    private int remainingStock() {
        return jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE sku_id = 100", Integer.class);
    }
}