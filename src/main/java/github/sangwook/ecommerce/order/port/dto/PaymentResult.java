package github.sangwook.ecommerce.order.port.dto;

public sealed interface PaymentResult {
    record PAYMENT_FAILED(PaymentFailedStage stage) implements PaymentResult {}
    record SUCCESS(String paymentKey) implements PaymentResult {}

    sealed interface PaymentFailedStage {
        record PAYMENT_INITIATE() implements PaymentFailedStage {}
        record PAYMENT_CONFIRM(String paymentKey) implements  PaymentFailedStage {}
    }
}
