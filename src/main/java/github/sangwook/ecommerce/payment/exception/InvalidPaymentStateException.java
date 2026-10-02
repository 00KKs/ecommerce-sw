package github.sangwook.ecommerce.payment.exception;

import lombok.Getter;

//내 서버의 payment 상태 전이 예외
@Getter
public class InvalidPaymentStateException extends RuntimeException {
    private final String currentStatus;

    public InvalidPaymentStateException(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}
