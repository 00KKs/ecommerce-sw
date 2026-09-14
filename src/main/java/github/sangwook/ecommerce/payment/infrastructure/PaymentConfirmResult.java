package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.exception.LocalFailureReasonCode;

public sealed interface PaymentConfirmResult {
    record SUCCESS(Long orderId, int amount) implements PaymentConfirmResult {}
    record FAILED(String reasonCode, String reasonMessage, boolean retryable) implements PaymentConfirmResult {
        public FAILED(LocalFailureReasonCode reason, boolean retryable) {
            this(reason.getCode(), reason.getMessage(), retryable);
        }
    }
    record UNKNOWN(Throwable cause) implements PaymentConfirmResult {}
}
