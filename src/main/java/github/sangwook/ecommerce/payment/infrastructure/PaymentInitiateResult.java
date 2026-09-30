package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.exception.LocalFailureReasonCode;

public sealed interface PaymentInitiateResult {

    record SUCCESS(String paymentKey, String orderId, int amount) implements PaymentInitiateResult {}
    record FAILED(String reasonCode, String reasonMessage) implements PaymentInitiateResult {
        public FAILED(LocalFailureReasonCode reason) {
            this(reason.getCode(), reason.getMessage());
        }
    }
    record UNKNOWN() implements PaymentInitiateResult {}

}
