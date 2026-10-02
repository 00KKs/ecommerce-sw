package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;

public enum InitiateScenario {
    SUCCESS {
        @Override
        PaymentInitiateResult apply(Long orderId, Integer amount) {
            return new PaymentInitiateResult.SUCCESS("fake-payment-key-" + orderId, String.valueOf(orderId), amount);
        }
    },
    FAIL {
        @Override
        PaymentInitiateResult apply(Long orderId, Integer amount) {
            return new PaymentInitiateResult.FAILED("CARD_DECLINED", "카드 승인이 거절되었습니다.");
        }
    },
    TIMEOUT {
        @Override
        PaymentInitiateResult apply(Long orderId, Integer amount) {
            return new PaymentInitiateResult.UNKNOWN();
        }
    };

    abstract PaymentInitiateResult apply(Long orderId, Integer amount);
}