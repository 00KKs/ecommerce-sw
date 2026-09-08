package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.exception.PaymentConfirmAmbiguousException;
import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;

public enum ConfirmScenario {
    SUCCESS {
        @Override
        PaymentConfirmResult apply(String paymentKey, Long orderId, int amount) {
            return new PaymentConfirmResult.SUCCESS(orderId, amount);
        }
    },
    ALWAYS_FAIL {
        @Override
        PaymentConfirmResult apply(String paymentKey, Long orderId, int amount) {
            return new PaymentConfirmResult.FAILED("PAYMENT_REJECTED", "결제가 거절되었습니다.", true);
        }
    },
    TIMEOUT {
        @Override
        PaymentConfirmResult apply(String paymentKey, Long orderId, int amount) {
            throw new PaymentConfirmAmbiguousException("결제 승인 타임아웃");
        }
    };

    abstract PaymentConfirmResult apply(String paymentKey, Long orderId, int amount);
}