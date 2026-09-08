package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;

public enum InitiateScenario {
    SUCCESS {
        @Override
        PaymentInitiateResult apply(Long orderId, Integer amount) {
            return new PaymentInitiateResult.SUCCESS("fake-payment-key-" + orderId);
        }
    },
    ALWAYS_FAIL {
        @Override
        PaymentInitiateResult apply(Long orderId, Integer amount) {
            return new PaymentInitiateResult.FAILED("CARD_DECLINED", "카드 승인이 거절되었습니다.", false);
        }
    },
    TIMEOUT {
        @Override
        PaymentInitiateResult apply(Long orderId, Integer amount) {
            return new PaymentInitiateResult.UNKNOWN(new IllegalStateException("결제 타임아웃 발생"));
        }
    };

    abstract PaymentInitiateResult apply(Long orderId, Integer amount);
}