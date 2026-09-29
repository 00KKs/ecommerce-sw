package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.application.PaymentGateway;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

import static github.sangwook.ecommerce.payment.exception.LocalFailureReasonCode.*;

@Component
@Slf4j
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

    /**
     * [흐름 대체] 결제창 인증 단계(원래는 클라이언트가 SDK로 수행)를 서버가 대신 수행한다.
     *
     * 실제 흐름: 클라이언트가 SDK로 결제창 인증 → paymentKey 수령 → 서버에 승인 요청 (클라이언트 → 서버)
     * 현재 구조: 클라이언트가 없으므로 서버가 Fake PG의 인증 API를 직접 호출해 paymentKey를 받는다. (서버 → PG)
     *
     * 실제 PG 연동 시 이 메서드는 제거되며, paymentKey는 승인 요청의 입력값으로 들어온다.
     * 이때 클라이언트 입력(orderId, amount)을 DB 기준으로 검증하는 로직이 승인 직전에 반드시 필요하다.
     */
    @Override
    public PaymentInitiateResult initiatePayment(Long orderId, Integer amount) {
        PaymentInitiateResponse initiateResponse = null;
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
        } catch (ResourceAccessException e) {
            return handleInitiateResourceAccessException(e);
        } catch (PaymentGatewayClientError e) {
            return new PaymentInitiateResult.FAILED(e.code, e.message, false);
        }

        if (initiateResponse == null) throw new IllegalStateException("결제 요청 중 오류가 발생했습니다.");
        if (!PaymentGatewayPaymentStatus.READY.name().equals(initiateResponse.status)) {
            log.warn("initiate 응답 status 비정상. orderId={}, status={}", orderId, initiateResponse.status);
            return new PaymentInitiateResult.UNKNOWN();
        }
        return new PaymentInitiateResult.SUCCESS(initiateResponse.paymentKey, initiateResponse.orderId, initiateResponse.amount);
    }

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        PaymentConfirmResponse confirmResponse = null;
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
                    .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        throw new PaymentGatewayServerError(response.getStatusCode(), body);
                    }))
                    .body(PaymentConfirmResponse.class);
        } catch (ResourceAccessException e) { //Spring은 ResourceAccessException로 I/O 에러를 감싼다.
            return handleConfirmResourceAccessException(e);
        } catch (PaymentGatewayClientError e) {
            return new PaymentConfirmResult.FAILED(e.code, e.message, false);
        } catch (PaymentGatewayServerError e) {
            log.error("PG사 서버 오류 발생. status={}, body={}", e.getStatusCode(), e.getBody());
            //재시도 가능
        } catch (Exception e) {
            log.error("결제 승인 중 오류 발생. 실제 예외 타입: {}, 메시지: {}", e.getClass().getName(), e.getMessage(), e);
            return new PaymentConfirmResult.UNKNOWN();
        }

        if (confirmResponse == null) {
            log.error("PG 응답 바디 비어있음. 승인 성공 여부 불명확. paymentKey={}, orderId={}", paymentKey, orderId);
            return new PaymentConfirmResult.UNKNOWN();
        }

        return new PaymentConfirmResult.SUCCESS(Long.valueOf(confirmResponse.orderId), confirmResponse.amount);
    }

    @Override
    public PaymentLookupResult lookupPayment(String paymentKey) {
        PaymentLookupResponse response = null;
        try {
            response = restClient
                    .get()
                    .uri("/v1/payments/{paymentKey}", paymentKey)
                    .retrieve()
                    .body(PaymentLookupResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("PG에 결제 내역 없음. paymentKey={}", paymentKey);
            return new PaymentLookupResult.NOT_FOUND();
        } catch (ResourceAccessException e) {
            log.error("I/O 오류 발생. paymentKey={}, cause={}", paymentKey, e.getCause(), e);
            return new PaymentLookupResult.UNAVAILABLE("IO ERROR");
        } catch (Exception e) {
            log.error("결제 조회 중 오류 발생. 실제 예외 타입: {}, 메시지: {}", e.getClass().getName(), e.getMessage(), e);
            return new PaymentLookupResult.UNAVAILABLE("LOOKUP FAILED");
        }

        if (response == null || response.status() == null) {
            log.error("결제 조회 응답 비어있음. paymentKey={}", paymentKey);
            return new PaymentLookupResult.UNAVAILABLE("EMPTY BODY");
        }
        return PaymentLookupResponseMapper.toResult(response);
    }

    private PaymentInitiateResult handleInitiateResourceAccessException(ResourceAccessException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ConnectTimeoutException || cause instanceof ConnectException) {
            //PG 서버 다운, 서킷브레이커 작동
            log.error("PG사 연결 실패. cause={}", cause.getClass().getName(), e);
            return new PaymentInitiateResult.FAILED(CONNECTION_REFUSED, false);
        } else if (cause instanceof ConnectionRequestTimeoutException) {
            //우리쪽 풀 고갈
            log.error("커넥션 풀 고갈, 풀 사이즈/트래픽 점검 필요. cause={}", cause.getClass().getName(), e);
            return new PaymentInitiateResult.FAILED(CONNECTION_POOL_EXHAUSTED, true); //지연이 필요하다를 추가해도 좋을 듯
        } else if (cause instanceof SocketTimeoutException) {
            log.error("응답 지연, 결제 요청 상태 불명, cause={}", cause.getClass().getName(), e);
            return new PaymentInitiateResult.UNKNOWN();
        } else if (cause instanceof SocketException) {
            //닫힌 소켓에 연결을 시도하거나, 상대방이 연결을 갑자기 끊은 경우
            log.error("소켓 예외, 연결이 예기치 않게 종료. cause={}", cause.getClass().getName(), e);
            return new PaymentInitiateResult.FAILED(CONNECTION_ABORTED, true);
        } else {
            log.error("미분류 I/O 오류 발생. cause={}", cause.getClass().getName(), e);
            return new PaymentInitiateResult.FAILED(UNCLASSIFIED_IO_ERROR, false);
        }
    }

    private PaymentConfirmResult handleConfirmResourceAccessException(ResourceAccessException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ConnectTimeoutException || cause instanceof ConnectException) {
            //PG 서버 다운, 서킷브레이커 작동
            log.error("PG사 연결 실패. cause={}", cause.getClass().getName(), e);
            return new PaymentConfirmResult.FAILED(CONNECTION_REFUSED, false);
        } else if (cause instanceof ConnectionRequestTimeoutException) {
            //우리쪽 풀 고갈
            log.error("커넥션 풀 고갈, 풀 사이즈/트래픽 점검 필요. cause={}", cause.getClass().getName(), e);
            return new PaymentConfirmResult.FAILED(CONNECTION_POOL_EXHAUSTED, true);
        } else if (cause instanceof SocketTimeoutException) {
            //연결은 가능, 응답을 주지 않거나 지연
            log.error("응답 지연, 결제 승인 상태 불명", e);
            //결제 승인 상태 확인 필요
            return new PaymentConfirmResult.UNKNOWN();
        } else if (cause instanceof SocketException) {
            //닫힌 소켓에 연결을 시도하거나, 상대방이 연결을 갑자기 끊은 경우
            log.error("소켓 예외, 연결이 예기치 않게 종료. cause={}", cause.getClass().getName(), e);
            return new PaymentConfirmResult.UNKNOWN();
        } else {
            log.error("미분류 I/O 오류 발생. cause={}", cause.getClass().getName(), e);
            return new PaymentConfirmResult.UNKNOWN();
        }
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

    @Getter
    private static class PaymentGatewayServerError extends RuntimeException {
        private final HttpStatusCode statusCode;
        private final String body;

        public PaymentGatewayServerError(HttpStatusCode statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
