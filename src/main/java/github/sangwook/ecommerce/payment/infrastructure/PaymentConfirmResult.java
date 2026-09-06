package github.sangwook.ecommerce.payment.infrastructure;

public sealed interface PaymentConfirmResult {
    record SUCCESS() implements PaymentConfirmResult {}
    record FAILED(String reasonCode, String reasonMessage) implements PaymentConfirmResult {}
    record UNKNOWN(Throwable cause) implements PaymentConfirmResult {}
}
