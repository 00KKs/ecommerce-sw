package github.sangwook.ecommerce.payment;

public sealed interface PaymentConfirmResult {
    record SUCCESS() implements PaymentConfirmResult {}
    record FAILED(String reasonCode, String reasonMessage) implements PaymentConfirmResult {}
    record UNKNOWN(Throwable cause) implements PaymentConfirmResult {}
}
