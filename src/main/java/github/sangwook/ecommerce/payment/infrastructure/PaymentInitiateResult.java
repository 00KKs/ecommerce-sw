package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.exception.LocalFailureReasonCode;

public sealed interface PaymentInitiateResult {

    record SUCCESS(String paymentKey) implements PaymentInitiateResult {}
    record FAILED(String reasonCode, String reasonMessage, boolean retryable) implements PaymentInitiateResult {
        public FAILED(LocalFailureReasonCode reason, boolean retryable) {
            this(reason.getCode(), reason.getMessage(), retryable);
        }
    }
    record UNKNOWN(Throwable cause) implements PaymentInitiateResult {}

}
