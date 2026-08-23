package github.sangwook.ecommerce.order.port.dto;

import lombok.Getter;

@Getter
public class PaymentResult {
    private final String paymentKey;
    private final Result result = Result.SUCCESS;

    public PaymentResult(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public enum Result {
        SUCCESS
    }
}
