package github.sangwook.ecommerce.order.port.dto;

public sealed interface PaymentResult {
    record FAILED() implements PaymentResult {}
    record SUCCESS(String paymentKey) implements PaymentResult {}
    record UNKNOWN() implements PaymentResult {}
}
