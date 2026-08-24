package github.sangwook.ecommerce.order.exception;

import lombok.Getter;

@Getter
public class OrderFailedException extends RuntimeException {

    public OrderFailedException(Throwable cause) {
        super(cause);
    }
}
