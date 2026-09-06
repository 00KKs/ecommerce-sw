package github.sangwook.ecommerce.payment.infrastructure;

public sealed interface PaymentInitiateResult {

    record SUCCESS(String paymentKey) implements PaymentInitiateResult {}
    record FAILED(String reasonCode, String reasonMessage) implements PaymentInitiateResult {}
    record UNKNOWN(Throwable cause) implements PaymentInitiateResult {}

}
