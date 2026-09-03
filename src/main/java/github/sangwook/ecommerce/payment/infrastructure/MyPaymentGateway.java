package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.PaymentGateway;
import github.sangwook.ecommerce.payment.PaymentInitiateResult;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class MyPaymentGateway implements PaymentGateway {

    private static final String PAYMENT_INITIATE_PATH = "/v1/payments";
    private static final String PAYMENT_CONFIRM_PATH = "/v1/payments/confirm";

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MyPaymentGateway(@Qualifier("PaymentGatewayRestClientBuilder") RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentInitiateResult initiatePayment(Long orderId, Integer amount) {
        PaymentInitiateResponse initiateResponse;
        try {
            initiateResponse = restClient
                    .post()
                    .uri(PAYMENT_INITIATE_PATH)
                    .body(new PaymentInitiateRequest(String.valueOf(orderId), amount))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
                        throw new PaymentGatewayClientError(errorResponse.code, errorResponse.message);
                    }))
                    .body(PaymentInitiateResponse.class);
        } catch (PaymentGatewayClientError e) {
            return new PaymentInitiateResult.FAILED(e.code, e.message);
        }

        if (initiateResponse == null) throw new IllegalStateException("결제 요청 중 오류가 발생했습니다.");

        //TODO amount 일치 여부, orderId 소유권/상태, status가 정말 READY인지 검증
        return new PaymentInitiateResult.SUCCESS(initiateResponse.paymentKey);
    }

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        PaymentConfirmResponse confirmResponse;
        try {
            confirmResponse = restClient
                    .post()
                    .uri(PAYMENT_CONFIRM_PATH)
                    .header(IDEMPOTENCY_HEADER, idempotencyKey.toString())
                    .body(new PaymentConfirmRequest(paymentKey, String.valueOf(orderId), amount))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
                        throw new PaymentGatewayClientError(errorResponse.code, errorResponse.message);
                    }))
                    .body(PaymentConfirmResponse.class);
        } catch (PaymentGatewayClientError e) {
            return new PaymentConfirmResult.FAILED(e.code, e.message);
        }
        if (confirmResponse == null) throw new IllegalStateException("결제 승인 중 오류가 발생했습니다.");

        //TODO 응답값 필드 검증
        return new PaymentConfirmResult.SUCCESS();
    }

    private record PaymentInitiateRequest(
        String orderId,
        int amount
    ){}

    private record PaymentInitiateResponse(
        String paymentKey,
        String orderId,
        String status,
        int amount
    ){}

    private record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        int amount
    ){}

    private record PaymentConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        int amount,
        OffsetDateTime approvedAt
    ){}

    private record ErrorResponse(
        String code,
        String message
    ){}

    @Getter
    private static class PaymentGatewayClientError extends RuntimeException {
        private final String code;
        private final String message;

        public PaymentGatewayClientError(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
