package github.sangwook.ecommerce.payment.infrastructure;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static github.sangwook.ecommerce.payment.exception.LocalFailureReasonCode.CONNECTION_POOL_EXHAUSTED;
import static github.sangwook.ecommerce.payment.exception.LocalFailureReasonCode.CONNECTION_REFUSED;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class MyPaymentGatewayTest {

    private ObjectMapper objectMapper = JsonMapper.builder().build();

    private MockWebServer server;
    private MyPaymentGateway gateway;

    private static final Long ORDER_ID = 1L;
    private static final int AMOUNT = 10_000;
    private static final String PAYMENT_KEY = "pk_test_1";
    private static final UUID IDEMPOTENCY_KEY = UUID.randomUUID();

    private static final Duration SHORT_RESPONSE_TIMEOUT = Duration.ofMillis(500);

    private static final String INITIATE_READY = """
              {"paymentKey":"pk_test_1","orderId":"1","status":"READY","amount":10000}
              """;
    private static final String CONFIRM_DONE = """
              {"paymentKey":"pk_test_1","orderId":"1","status":"DONE","amount":10000,"approvedAt":"2026-10-15T10:00:00+09:00"}
              """;
    private static final String PROVIDER_ERROR = """
              {"code":"PROVIDER_ERROR","message":"일시적인 오류가 발생했습니다."}
              """;
    private static final String LOOKUP_DONE = """
              {"paymentKey":"pk_test_1","orderId":"1","status":"DONE","amount":10000,
               "approvedAt":"2026-10-15T10:00:00+09:00","canceledAt":null}
              """;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        gateway = gatewayFor(baseUrl(server), 10, Duration.ofSeconds(1), SHORT_RESPONSE_TIMEOUT);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Nested
    class 결제_요청_initiate {

        @Test
        @DisplayName("READY 응답이면 SUCCESS를 반환하고, 요청 경로와 바디가 올바르다")
        void success() throws Exception {
            server.enqueue(json(200, INITIATE_READY));

            PaymentInitiateResult result = gateway.initiatePayment(ORDER_ID, AMOUNT);

            assertThat(result).isEqualTo(new PaymentInitiateResult.SUCCESS(PAYMENT_KEY, "1", AMOUNT));
            RecordedRequest request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/v1/payments");
            assertThat(request.getBody().readUtf8()).isEqualTo("{\"orderId\":\"1\",\"amount\":10000}");
        }

        @Test
        @DisplayName("200이지만 status가 READY가 아니면 UNKNOWN을 반환한다")
        void notReadyStatus() {
            server.enqueue(json(200, """
                      {"paymentKey":"pk_test_1","orderId":"1","status":"DONE","amount":10000}
                      """));

            assertThat(gateway.initiatePayment(ORDER_ID, AMOUNT)).isEqualTo(new PaymentInitiateResult.UNKNOWN());
        }

        @Test
        @DisplayName("200이지만 바디가 비어있으면 UNKNOWN을 반환한다")
        void emptyBody() {
            server.enqueue(new MockResponse().setResponseCode(200));

            assertThat(gateway.initiatePayment(ORDER_ID, AMOUNT)).isEqualTo(new PaymentInitiateResult.UNKNOWN());
        }

        @Test
        @DisplayName("4xx 응답이면 PG의 에러 코드와 메시지를 담은 FAILED를 반환한다")
        void clientError() {
            server.enqueue(json(400, """
                      {"code":"INVALID_REQUEST","message":"잘못된 요청입니다."}
                      """));

            assertThat(gateway.initiatePayment(ORDER_ID, AMOUNT))
                    .isEqualTo(new PaymentInitiateResult.FAILED("INVALID_REQUEST", "잘못된 요청입니다."));
        }

        @Test
        @DisplayName("5xx 응답이면 예외를 전파하지 않고 UNKNOWN을 반환한다")
        void serverError() {
            server.enqueue(json(500, "{\"message\":\"internal error\"}"));

            assertThat(gateway.initiatePayment(ORDER_ID, AMOUNT)).isEqualTo(new PaymentInitiateResult.UNKNOWN());
        }

        @Test
        @DisplayName("응답 타임아웃이면 요청 처리 여부를 알 수 없으므로 UNKNOWN을 반환한다")
        void responseTimeout() {
            server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

            assertThat(gateway.initiatePayment(ORDER_ID, AMOUNT)).isEqualTo(new PaymentInitiateResult.UNKNOWN());
        }

        @Test
        @DisplayName("PG 서버에 연결할 수 없으면 FAILED(CONNECTION_REFUSED)를 반환한다")
        void connectionRefused() throws IOException {
            MyPaymentGateway deadGateway = gatewayFor(closedServerUrl(), 10, Duration.ofSeconds(1), SHORT_RESPONSE_TIMEOUT);

            assertThat(deadGateway.initiatePayment(ORDER_ID, AMOUNT))
                    .isEqualTo(new PaymentInitiateResult.FAILED(CONNECTION_REFUSED));
        }
    }

    @Nested
    class 결제_승인_confirm {

        @Test
        @DisplayName("성공 응답이면 SUCCESS를 반환하고, 멱등키 헤더와 바디가 올바르다")
        void success() throws Exception {
            server.enqueue(json(200, CONFIRM_DONE));

            PaymentConfirmResult result = confirm(gateway);

            assertThat(result).isEqualTo(new PaymentConfirmResult.SUCCESS(ORDER_ID, AMOUNT));
            RecordedRequest request = server.takeRequest();
            assertThat(request.getPath()).isEqualTo("/v1/payments/confirm");
            assertThat(request.getHeader("Idempotency-Key")).isEqualTo(IDEMPOTENCY_KEY.toString());
            assertThat(request.getBody().readUtf8())
                    .isEqualTo("{\"paymentKey\":\"pk_test_1\",\"orderId\":\"1\",\"amount\":10000}");
        }

        @Test
        @DisplayName("200이지만 바디가 비어있으면 승인 여부를 알 수 없으므로 UNKNOWN을 반환한다")
        void emptyBody() {
            server.enqueue(new MockResponse().setResponseCode(200));

            assertThat(confirm(gateway)).isEqualTo(new PaymentConfirmResult.UNKNOWN());
        }

        @Test
        @DisplayName("재시도 불가능한 4xx면 재시도 없이 FAILED(retryable=false)를 반환한다")
        void nonRetryableClientError() {
            server.enqueue(json(400, """
                      {"code":"REJECT_CARD_PAYMENT","message":"한도가 초과되었습니다."}
                      """));

            assertThat(confirm(gateway))
                    .isEqualTo(new PaymentConfirmResult.FAILED("REJECT_CARD_PAYMENT", "한도가 초과되었습니다.", false));
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("PROVIDER_ERROR가 계속되면 최초 1회 + 재시도 3회 후 FAILED(retryable=false)로 바꿔 반환한다")
        void retryExhausted() {
            for (int i = 0; i < 4; i++) {
                server.enqueue(json(400, PROVIDER_ERROR));
            }

            PaymentConfirmResult result = confirm(gateway);

            assertThat(result).isEqualTo(
                    new PaymentConfirmResult.FAILED("PROVIDER_ERROR", "일시적인 오류가 발생했습니다.", false));
            assertThat(server.getRequestCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("재시도 요청은 모두 최초 요청과 같은 멱등키를 사용한다")
        void retryReusesIdempotencyKey() throws Exception {
            for (int i = 0; i < 4; i++) {
                server.enqueue(json(400, PROVIDER_ERROR));
            }

            confirm(gateway);

            for (int i = 0; i < 4; i++) {
                assertThat(server.takeRequest().getHeader("Idempotency-Key")).isEqualTo(IDEMPOTENCY_KEY.toString());
            }
        }

        @Test
        @DisplayName("PROVIDER_ERROR 후 재시도에서 성공하면 SUCCESS를 반환하고 추가 재시도하지 않는다")
        void retryThenSuccess() {
            server.enqueue(json(400, PROVIDER_ERROR));
            server.enqueue(json(400, PROVIDER_ERROR));
            server.enqueue(json(200, CONFIRM_DONE));

            assertThat(confirm(gateway)).isEqualTo(new PaymentConfirmResult.SUCCESS(ORDER_ID, AMOUNT));
            assertThat(server.getRequestCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("5xx면 승인 여부를 확신할 수 없으므로 재시도 없이 UNKNOWN을 반환한다")
        void serverError() {
            server.enqueue(json(500, "{\"message\":\"internal error\"}"));

            assertThat(confirm(gateway)).isEqualTo(new PaymentConfirmResult.UNKNOWN());
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("응답 타임아웃이면 재시도 없이 UNKNOWN을 반환한다")
        void responseTimeout() {
            server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

            assertThat(confirm(gateway)).isEqualTo(new PaymentConfirmResult.UNKNOWN());
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("요청 수신 후 응답 없이 연결이 끊기면 재시도 없이 UNKNOWN을 반환한다")
        void disconnectAfterRequest() {
            server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));

            assertThat(confirm(gateway)).isEqualTo(new PaymentConfirmResult.UNKNOWN());
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("PG 서버에 연결할 수 없으면 재시도 없이 FAILED(CONNECTION_REFUSED, retryable=false)를 반환한다")
        void connectionRefused() throws IOException {
            MyPaymentGateway deadGateway = gatewayFor(closedServerUrl(), 10, Duration.ofSeconds(1), SHORT_RESPONSE_TIMEOUT);

            assertThat(confirm(deadGateway)).isEqualTo(new PaymentConfirmResult.FAILED(CONNECTION_REFUSED, false));
        }

        @Test
        @DisplayName("커넥션 풀이 고갈되면 재시도 후 FAILED(CONNECTION_POOL_EXHAUSTED, retryable=false)를 반환한다")
        void connectionPoolExhausted() throws Exception {
            // 풀 크기 1, 풀 대기 200ms. 응답 타임아웃은 점유 요청이 먼저 끊기지 않도록 길게 잡는다.
            MyPaymentGateway singleConnectionGateway =
                    gatewayFor(baseUrl(server), 1, Duration.ofMillis(200), Duration.ofSeconds(10));

            // 재시도 총 대기(0.5+1+2초)보다 오래 커넥션을 점유하도록 응답을 6초 지연
            server.enqueue(json(200, LOOKUP_DONE).setHeadersDelay(6, TimeUnit.SECONDS));
            CompletableFuture<PaymentLookupResult> holder =
                    CompletableFuture.supplyAsync(() -> singleConnectionGateway.lookupPayment("holder"));
            assertThat(server.takeRequest(2, TimeUnit.SECONDS)).isNotNull(); // 커넥션 점유 확인

            PaymentConfirmResult result = confirm(singleConnectionGateway);

            assertThat(result).isEqualTo(new PaymentConfirmResult.FAILED(CONNECTION_POOL_EXHAUSTED, false));
            assertThat(server.getRequestCount()).isEqualTo(1); // confirm은 PG까지 도달하지 못했다
            holder.get(10, TimeUnit.SECONDS);
        }
    }

    @Nested
    class 결제_조회_lookup {

        @Test
        @DisplayName("DONE 응답이면 금액과 승인 시각을 담은 DONE을 반환한다")
        void done() throws Exception {
            server.enqueue(json(200, LOOKUP_DONE));

            PaymentLookupResult result = gateway.lookupPayment(PAYMENT_KEY);

            assertThat(result).isInstanceOf(PaymentLookupResult.DONE.class);
            PaymentLookupResult.DONE done = (PaymentLookupResult.DONE) result;
            assertThat(done.amount()).isEqualTo(AMOUNT);
            assertThat(done.approvedAt()).isAtSameInstantAs(OffsetDateTime.parse("2026-10-15T10:00:00+09:00"));
            assertThat(server.takeRequest().getPath()).isEqualTo("/v1/payments/" + PAYMENT_KEY);
        }

        @Test
        @DisplayName("READY 응답이면 READY를 반환한다")
        void ready() {
            server.enqueue(json(200, lookupBody("READY", null, null)));

            assertThat(gateway.lookupPayment(PAYMENT_KEY)).isEqualTo(new PaymentLookupResult.READY());
        }

        @Test
        @DisplayName("ABORTED 응답이면 ABORTED를 반환한다")
        void aborted() {
            server.enqueue(json(200, lookupBody("ABORTED", null, null)));

            assertThat(gateway.lookupPayment(PAYMENT_KEY)).isEqualTo(new PaymentLookupResult.ABORTED());
        }

        @Test
        @DisplayName("CANCELED 응답이면 승인/취소 시각을 담은 CANCELED를 반환한다")
        void canceled() {
            server.enqueue(json(200, lookupBody("CANCELED", "\"2026-10-15T10:00:00+09:00\"", "\"2026-10-15T11:00:00+09:00\"")));

            assertThat(gateway.lookupPayment(PAYMENT_KEY)).isInstanceOf(PaymentLookupResult.CANCELED.class);
        }

        @Test
        @DisplayName("404면 NOT_FOUND를 반환한다")
        void notFound() {
            server.enqueue(json(404, "{\"code\":\"NOT_FOUND_PAYMENT\",\"message\":\"존재하지 않는 결제\"}"));

            assertThat(gateway.lookupPayment(PAYMENT_KEY)).isEqualTo(new PaymentLookupResult.NOT_FOUND());
        }

        @Test
        @DisplayName("5xx면 UNAVAILABLE을 반환한다")
        void serverError() {
            server.enqueue(json(500, "{\"message\":\"internal error\"}"));

            assertThat(gateway.lookupPayment(PAYMENT_KEY)).isEqualTo(new PaymentLookupResult.UNAVAILABLE("LOOKUP FAILED"));
        }

        @Test
        @DisplayName("응답 타임아웃이면 UNAVAILABLE(IO ERROR)을 반환한다")
        void responseTimeout() {
            server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

            assertThat(gateway.lookupPayment(PAYMENT_KEY)).isEqualTo(new PaymentLookupResult.UNAVAILABLE("IO ERROR"));
        }

        @Test
        @DisplayName("200이지만 바디가 비어있으면 UNAVAILABLE(EMPTY BODY)을 반환한다")
        void emptyBody() {
            server.enqueue(new MockResponse().setResponseCode(200));

            assertThat(gateway.lookupPayment(PAYMENT_KEY)).isEqualTo(new PaymentLookupResult.UNAVAILABLE("EMPTY BODY"));
        }

        @Test
        @DisplayName("알 수 없는 status 값이 오면 예외를 전파하지 않고 UNAVAILABLE을 반환한다")
        void unknownStatus() {
            server.enqueue(json(200, lookupBody("WAITING_FOR_DEPOSIT", null, null)));

            assertThat(gateway.lookupPayment(PAYMENT_KEY)).isEqualTo(new PaymentLookupResult.UNAVAILABLE("LOOKUP FAILED"));
        }
    }

    private PaymentConfirmResult confirm(MyPaymentGateway target) {
        return target.confirmPayment(PAYMENT_KEY, ORDER_ID, AMOUNT, IDEMPOTENCY_KEY);
    }

    private MyPaymentGateway gatewayFor(String baseUrl, int maxConnections,
                                        Duration connectionRequestTimeout, Duration responseTimeout) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(1))
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(maxConnections)
                .setMaxConnPerRoute(maxConnections)
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionRequestTimeout.toMillis()))
                .setResponseTimeout(Timeout.ofMilliseconds(responseTimeout.toMillis()))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .baseUrl(baseUrl);

        io.github.resilience4j.retry.RetryConfig config = io.github.resilience4j.retry.RetryConfig.<PaymentConfirmResult>custom()
                .maxAttempts(3 + 1) //최초 시도도 포함되어 +1
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(500),
                        2.0, // 500 -> 1000 -> 2000
                        Duration.ofSeconds(2) //maxInterval
                ))
                .retryOnResult(result -> result instanceof PaymentConfirmResult.FAILED failed && failed.retryable())
                .retryOnException(e -> false)
                .failAfterMaxAttempts(false)
                .build();

        Retry retry = Retry.of("paymentConfirm", config);
        retry.getEventPublisher()
                .onRetry(event -> log.warn("결제 승인 재시도. attempt={}, wait={}ms", event.getNumberOfRetryAttempts(), event.getWaitInterval().toMillis()));
        return new MyPaymentGateway(builder, objectMapper, retry);
    }

    private static String baseUrl(MockWebServer target) {
        return "http://localhost:" + target.getPort();
    }

    private static String closedServerUrl() throws IOException {
        MockWebServer closed = new MockWebServer();
        closed.start();
        String url = baseUrl(closed);
        closed.shutdown();
        return url;
    }

    private static MockResponse json(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private static String lookupBody(String status, String approvedAt, String canceledAt) {
        return """
                  {"paymentKey":"pk_test_1","orderId":"1","status":"%s","amount":10000,"approvedAt":%s,"canceledAt":%s}
                  """.formatted(status, approvedAt, canceledAt);
    }

}
