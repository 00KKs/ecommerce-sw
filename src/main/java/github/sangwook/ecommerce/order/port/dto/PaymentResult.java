package github.sangwook.ecommerce.order.port.dto;

import jakarta.annotation.Nullable;

public sealed interface PaymentResult {
    record CONFIRMED(String paymentKey) implements PaymentResult {}
    record FAILED() implements PaymentResult {}
    record PENDING(@Nullable String paymentKey) implements PaymentResult {}

    default @Nullable String paymentKey() {
        return switch (this) {
            case CONFIRMED(String paymentKey) -> paymentKey;
            case FAILED() -> null;
            case PENDING(String paymentKey) -> paymentKey;
        };
    }
}

