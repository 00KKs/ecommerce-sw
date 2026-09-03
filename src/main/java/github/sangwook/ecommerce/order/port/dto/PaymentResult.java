package github.sangwook.ecommerce.order.port.dto;

public sealed interface PaymentResult {
    record PAYMENT_FAILED() implements PaymentResult {}
    record SUCCESS(String paymentKey) implements PaymentResult {}
    record UNKNOWN() implements PaymentResult {}
}
