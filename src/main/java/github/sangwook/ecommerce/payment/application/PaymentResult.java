package github.sangwook.ecommerce.payment.application;

import jakarta.annotation.Nullable;

public sealed interface PaymentResult {
    record PAYMENT_FAILED(PaymentFailedStage stage, boolean retryable) implements PaymentResult {}
    record SUCCESS(String paymentKey) implements PaymentResult {}
    record PAYMENT_UNKNOWN(String paymentKey) implements PaymentResult {}

    sealed interface PaymentFailedStage {
        record PAYMENT_INITIATE() implements PaymentFailedStage {}
        record PAYMENT_CONFIRM(String paymentKey) implements  PaymentFailedStage {}
    }

    default @Nullable String paymentKey() {
        return switch (this) {
            case SUCCESS(String paymentKey) -> paymentKey;
            case PAYMENT_UNKNOWN(String paymentKey) -> paymentKey;
            case PAYMENT_FAILED(PaymentFailedStage stage, boolean retryable) -> switch (stage) {
                case PaymentFailedStage.PAYMENT_INITIATE initiate -> null;
                case PaymentFailedStage.PAYMENT_CONFIRM(String paymentKey) -> paymentKey;
            };
        };
    }
}

