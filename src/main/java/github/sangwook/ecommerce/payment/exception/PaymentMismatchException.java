package github.sangwook.ecommerce.payment.exception;

import lombok.Getter;

//클라이언트로부터 받은 결제 정보가 우리 서버와 불일치할때 발생하는 예외
@Getter
public class PaymentMismatchException extends RuntimeException {
    private final String field;
    private final String expected;
    private final String actual;

    public PaymentMismatchException(String field, String expected, String actual) {
        this.field = field;
        this.expected = expected;
        this.actual = actual;
    }
}
