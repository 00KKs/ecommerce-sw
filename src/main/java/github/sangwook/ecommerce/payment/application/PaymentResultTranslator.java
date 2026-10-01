package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.order.port.dto.PaymentResult.CONFIRMED;
import github.sangwook.ecommerce.order.port.dto.PaymentResult.FAILED;
import github.sangwook.ecommerce.order.port.dto.PaymentResult.PENDING;
import github.sangwook.ecommerce.payment.application.PaymentResult.PAYMENT_FAILED;
import github.sangwook.ecommerce.payment.application.PaymentResult.PAYMENT_UNKNOWN;
import github.sangwook.ecommerce.payment.application.PaymentResult.SUCCESS;

public class PaymentResultTranslator {

    public static PaymentResult translate(github.sangwook.ecommerce.payment.application.PaymentResult paymentResult) {
        return switch (paymentResult) {
            case SUCCESS(String paymentKey) -> new CONFIRMED(paymentKey);
            case PAYMENT_FAILED ignored -> new FAILED();
            case PAYMENT_UNKNOWN(String paymentKey) -> new PENDING(paymentKey);
        };
    }

}
