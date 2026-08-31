package github.sangwook.ecommerce.order.port.dto;

import lombok.Getter;

@Getter
public class PaymentResult {
    private final String paymentKey;
    private final Result result;

    public PaymentResult(String paymentKey, Result result) {
        this.paymentKey = paymentKey;
        this.result = result;
    }

    public enum Result {
        SUCCESS,
        FAILED
    }
}
