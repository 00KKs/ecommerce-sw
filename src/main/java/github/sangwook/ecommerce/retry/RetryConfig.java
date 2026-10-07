package github.sangwook.ecommerce.retry;

import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class RetryConfig {

    private static final int MAX_RETRIES = 3;

    @Bean(name = "paymentConfirmRetry")
    public Retry paymentConfirmRetry() {
        io.github.resilience4j.retry.RetryConfig config = io.github.resilience4j.retry.RetryConfig.<PaymentConfirmResult>custom()
                .maxAttempts(MAX_RETRIES + 1) //최초 시도도 포함되어 +1
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(500),
                        2.0, // 500 -> 1000 -> 2000
                        Duration.ofSeconds(2) //maxInterval
                ))
                .retryOnResult(result -> result instanceof PaymentConfirmResult.FAILED failed && failed.retryable())
                .retryOnException(e -> false)
                .failAfterMaxAttempts(false)
                .build();

        Retry retry = Retry.of("paymentConfirm", config);
        retry.getEventPublisher()
                .onRetry(event -> log.warn("결제 승인 재시도. attempt={}, wait={}ms", event.getNumberOfRetryAttempts(), event.getWaitInterval().toMillis()));
        return retry;

    }
}
