package github.sangwook.ecommerce.payment.exception;

public class PaymentConfirmAmbiguousException extends RuntimeException {

    public PaymentConfirmAmbiguousException(String message) {
        super(message);
    }

    public PaymentConfirmAmbiguousException(String message, Throwable cause) {
        super(message, cause);
    }
}
