package github.sangwook.ecommerce.payment.infrastructure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentGateway {

    private static final String PAYMENT_INITIATE_PATH = "/v1/payments";

    private final RestClient restClient;

    public PaymentGateway(@Qualifier("PaymentGatewayRestClientBuilder") RestClient.Builder builder) {
        this.restClient = builder.build();
    }

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
}
