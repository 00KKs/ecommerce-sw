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
    RETRYABLE_FAILURE {
        @Override
        PaymentInitiateResult apply(Long orderId, Integer amount) {
            return new PaymentInitiateResult.FAILED("FAILED_INTERNAL_SYSTEM_PROCESSING", "내부 시스템 처리 작업이 실패했습니다. 잠시 후 다시 시도해주세요.", true);
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