package github.sangwook.ecommerce.payment.application;

import jakarta.annotation.Nullable;

public sealed interface PaymentResult {
    record PAYMENT_INITIATE_FAILED() implements PaymentResult {}
    record PAYMENT_CONFIRM_FAILED(@Nullable String paymentKey, boolean retryable) implements PaymentResult {}
    record SUCCESS(String paymentKey) implements PaymentResult {}
    record PAYMENT_UNKNOWN(String paymentKey) implements PaymentResult {}

    default @Nullable String paymentKey() {
        return switch (this) {
            case SUCCESS(String paymentKey) -> paymentKey;
            case PAYMENT_UNKNOWN(String paymentKey) -> paymentKey;
            case PAYMENT_CONFIRM_FAILED(String paymentKey, boolean retryable) -> paymentKey;
            case PAYMENT_INITIATE_FAILED() -> null;
        };
    }
}

