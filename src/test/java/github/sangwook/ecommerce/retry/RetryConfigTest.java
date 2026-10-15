package github.sangwook.ecommerce.retry;

import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RetryConfigTest {
    private static final PaymentConfirmResult SUCCESS = new PaymentConfirmResult.SUCCESS(1L, 10_000);
    private static final PaymentConfirmResult RETRYABLE = new PaymentConfirmResult.FAILED("PROVIDER_ERROR", "일시적 오류", true);
    private static final PaymentConfirmResult NON_RETRYABLE = new PaymentConfirmResult.FAILED("REJECT_CARD_PAYMENT", "한도 초과", false);
    private static final PaymentConfirmResult UNKNOWN = new PaymentConfirmResult.UNKNOWN();

    private Retry retry;

    @BeforeEach
    void setUp() {
        retry = new RetryConfig().paymentConfirmRetry();
    }

    @Nested
    class 재시도_대상이_아닌_결과 {

        @Test
        @DisplayName("SUCCESS는 재시도하지 않는다")
        void success() {
            FakeConfirmCall confirm = FakeConfirmCall.sequence(SUCCESS);

            assertThat(retry.executeSupplier(confirm)).isEqualTo(SUCCESS);
            assertThat(confirm.calls()).isEqualTo(1);
        }

        @Test
        @DisplayName("retryable=false인 FAILED는 재시도하지 않는다")
        void nonRetryableFailure() {
            FakeConfirmCall confirm = FakeConfirmCall.sequence(NON_RETRYABLE);

            assertThat(retry.executeSupplier(confirm)).isEqualTo(NON_RETRYABLE);
            assertThat(confirm.calls()).isEqualTo(1);
        }

        @Test
        @DisplayName("UNKNOWN은 승인 여부가 불명확하므로 재시도하지 않고 조회 흐름에 넘긴다")
        void unknown() {
            FakeConfirmCall confirm = FakeConfirmCall.sequence(UNKNOWN);

            assertThat(retry.executeSupplier(confirm)).isEqualTo(UNKNOWN);
            assertThat(confirm.calls()).isEqualTo(1);
        }

        @Test
        @DisplayName("예외는 재시도하지 않고 그대로 전파한다")
        void exception() {
            AtomicInteger calls = new AtomicInteger();
            Supplier<PaymentConfirmResult> throwing = () -> {
                calls.incrementAndGet();
                throw new IllegalStateException("예상치 못한 오류");
            };

            assertThatThrownBy(() -> retry.executeSupplier(throwing)).isInstanceOf(IllegalStateException.class);
            assertThat(calls.get()).isEqualTo(1);
        }
    }

    @Nested
    class 재시도_가능한_실패 {

        @Test
        @DisplayName("retryable 실패 후 성공하면 그 시점에 멈추고 SUCCESS를 반환한다")
        void retryThenSuccess() {
            FakeConfirmCall confirm = FakeConfirmCall.sequence(RETRYABLE, RETRYABLE, SUCCESS);

            assertThat(retry.executeSupplier(confirm)).isEqualTo(SUCCESS);
            assertThat(confirm.calls()).isEqualTo(3);
        }

        @Test
        @DisplayName("재시도 중 UNKNOWN이 나오면 더 재시도하지 않고 UNKNOWN을 반환한다")
        void retryThenUnknown() {
            FakeConfirmCall confirm = FakeConfirmCall.sequence(RETRYABLE, UNKNOWN);

            assertThat(retry.executeSupplier(confirm)).isEqualTo(UNKNOWN);
            assertThat(confirm.calls()).isEqualTo(2);
        }

        @Test
        @DisplayName("재시도 중 retryable=false 실패가 나오면 더 재시도하지 않는다")
        void retryThenNonRetryable() {
            FakeConfirmCall confirm = FakeConfirmCall.sequence(RETRYABLE, NON_RETRYABLE);

            assertThat(retry.executeSupplier(confirm)).isEqualTo(NON_RETRYABLE);
            assertThat(confirm.calls()).isEqualTo(2);
        }
    }

    @Nested
    class 재시도_소진 {

        @Test
        @DisplayName("최초 1회 + 재시도 3회 = 총 4회 시도한다")
        void maxAttempts() {
            FakeConfirmCall confirm = FakeConfirmCall.always(RETRYABLE);

            retry.executeSupplier(confirm);

            assertThat(confirm.calls()).isEqualTo(4);
        }

        @Test
        @DisplayName("소진되면 예외 없이 마지막 결과(retryable=true 그대로)를 반환한다 - false 변환은 MyPaymentGateway 책임")
        void returnsLastResultWithoutException() {
            FakeConfirmCall confirm = FakeConfirmCall.always(RETRYABLE);

            PaymentConfirmResult result = retry.executeSupplier(confirm);

            assertThat(result).isEqualTo(RETRYABLE);
        }

        @Test
        @DisplayName("재시도 대기 시간은 500ms -> 1000ms -> 2000ms로 늘어난다")
        void exponentialBackoff() {
            List<Duration> waits = new CopyOnWriteArrayList<>();
            retry.getEventPublisher().onRetry(event -> waits.add(event.getWaitInterval()));
            FakeConfirmCall confirm = FakeConfirmCall.always(RETRYABLE);

            long start = System.nanoTime();
            retry.executeSupplier(confirm);
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertThat(waits).containsExactly(
                    Duration.ofMillis(500),
                    Duration.ofMillis(1000),
                    Duration.ofMillis(2000));
            assertThat(elapsedMillis).isGreaterThanOrEqualTo(3_500); // 이벤트 값만이 아니라 실제로 대기했는지
        }

        @Test
        @DisplayName("싱글톤 Retry를 여러 요청이 동시에 써도 시도 횟수는 요청마다 독립적으로 센다")
        void independentPerCall() throws Exception {
            FakeConfirmCall first = FakeConfirmCall.always(RETRYABLE);
            FakeConfirmCall second = FakeConfirmCall.always(RETRYABLE);

            CompletableFuture<PaymentConfirmResult> f1 = CompletableFuture.supplyAsync(() -> retry.executeSupplier(first));
            CompletableFuture<PaymentConfirmResult> f2 = CompletableFuture.supplyAsync(() -> retry.executeSupplier(second));
            CompletableFuture.allOf(f1, f2).get(10, TimeUnit.SECONDS);

            assertThat(first.calls()).isEqualTo(4);
            assertThat(second.calls()).isEqualTo(4);
        }
    }

    /**
     * 호출마다 스크립트의 결과를 순서대로 반환하고, 마지막 결과는 이후 호출에서도 계속 반환한다.
     */
    private static class FakeConfirmCall implements Supplier<PaymentConfirmResult> {

        private final List<PaymentConfirmResult> results;
        private final boolean repeatLast;
        private int calls = 0;

        private FakeConfirmCall(List<PaymentConfirmResult> results, boolean repeatLast) {
            if (results.isEmpty()) {
                throw new IllegalArgumentException("결과를 최소 1개 지정해야 한다");
            }
            this.results = results;
            this.repeatLast = repeatLast;
        }

        static FakeConfirmCall always(PaymentConfirmResult result) {
            return new FakeConfirmCall(List.of(result), true);
        }

        static FakeConfirmCall sequence(PaymentConfirmResult... results) {
            return new FakeConfirmCall(List.of(results), false);
        }

        @Override
        public PaymentConfirmResult get() {
            calls++;
            if (calls <= results.size()) {
                return results.get(calls - 1);
            }
            if (repeatLast) {
                return results.getLast();
            }
            throw new AssertionError("예상보다 많이 호출됨: " + calls + "회 (준비된 결과 " + results.size() + "개)");
        }

        int calls() {
            return calls;
        }
    }
}
