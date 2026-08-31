package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.PaymentGateway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class MyPaymentGateway implements PaymentGateway {

    private static final String PAYMENT_INITIATE_PATH = "/v1/payments";
    private static final String PAYMENT_CONFIRM_PATH = "/v1/payments/confirm";

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final RestClient restClient;

    public MyPaymentGateway(@Qualifier("PaymentGatewayRestClientBuilder") RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public String initiatePayment(Long orderId, Integer amount) {
        PaymentInitiateResponse response;
        try {
            response = restClient
                    .post()
                    .uri(PAYMENT_INITIATE_PATH)
                    .body(new PaymentInitiateRequest(String.valueOf(orderId), amount))
                    .retrieve()
                    .body(PaymentInitiateResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("결제 요청 중 오류가 발생했습니다.");
        }

        if (response == null) throw new IllegalStateException("결제 요청 중 오류가 발생했습니다.");

        //TODO amount 일치 여부, orderId 소유권/상태, status가 정말 READY인지 검증
        return response.paymentKey;
    }

    @Override
    public PaymentStatus confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        PaymentConfirmResponse response;
        try {
            response = restClient
                    .post()
                    .uri(PAYMENT_CONFIRM_PATH)
                    .header(IDEMPOTENCY_HEADER, idempotencyKey.toString())
                    .body(new PaymentConfirmRequest(paymentKey, String.valueOf(orderId), amount))
                    .retrieve()
                    .body(PaymentConfirmResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("결제 승인 중 오류가 발생했습니다.");
        }
        if (response == null) throw new IllegalStateException("결제 승인 중 오류가 발생했습니다.");

        //TODO 응답값 필드 검증
        return PaymentStatus.valueOf(response.status);
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
}
