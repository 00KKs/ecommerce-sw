package github.sangwook.ecommerce.payment.infrastructure;

public sealed interface PaymentConfirmResult {
    record SUCCESS(Long orderId, int amount) implements PaymentConfirmResult {}
    record FAILED(String reasonCode, String reasonMessage, boolean retryable) implements PaymentConfirmResult {}
    record UNKNOWN(Throwable cause) implements PaymentConfirmResult {}
}
