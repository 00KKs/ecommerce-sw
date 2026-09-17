package github.sangwook.ecommerce.payment.application;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PaymentLookupRetryPolicy {

    private static final int MAX_RETRY_COUNT = 4;

    //FIXME jitter 편차폭이 10초에 비해 너무 적음
    public long nextDelaySeconds(int checkCount) {
        long base = Math.min(60, 5L << checkCount);
        long jitter = ThreadLocalRandom.current().nextLong(0, base / 2 + 1);
        return base + jitter;
    }

    public int maxRetryCount() {
        return MAX_RETRY_COUNT;
    }
}
