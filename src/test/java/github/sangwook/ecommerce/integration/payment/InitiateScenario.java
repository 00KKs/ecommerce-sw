package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

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
    },
    EXCEPTION {
        @Override
        PaymentInitiateResult apply(Long orderId, Integer amount) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    };

    abstract PaymentInitiateResult apply(Long orderId, Integer amount);
}