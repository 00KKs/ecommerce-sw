package github.sangwook.ecommerce.timeout;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

class TimeoutExceptionVerificationTest {

    private ServerSocket serverSocket;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        executor.shutdownNow();
    }

    private RestClient buildClient(String baseUrl, int connectTimeoutSec,
                                    int connReqTimeoutSec, int responseTimeoutSec,
                                    int maxTotal, int maxPerRoute) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(connectTimeoutSec))
            .build();

        PoolingHttpClientConnectionManager connectionManager =
            PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnTotal(maxTotal)
                .setMaxConnPerRoute(maxPerRoute)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(connReqTimeoutSec))
            .setResponseTimeout(Timeout.ofSeconds(responseTimeoutSec))
            .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

        return RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
            .baseUrl(baseUrl)
            .build();
    }

    /**
     * 1. connectTimeout 재현
     * 라우팅은 되지만 응답이 없는 IP(TEST-NET, 예약 대역)로 붙여서
     * SYN에 대한 SYN-ACK/RST가 영원히 안 오는 상황을 만듦.
     */
    @Test
    void connectTimeout_예외_확인() {
        // 192.0.2.0/24는 RFC 5737 예약 대역(TEST-NET-1) — 실존 응답 없음
        RestClient client = buildClient("http://192.0.2.1:9999", 2, 3, 5, 10, 10);

        assertThatThrownBy(() ->
            client.get().retrieve().toBodilessEntity()
        )
        .isInstanceOf(ResourceAccessException.class)
        .cause()
        .isInstanceOf(ConnectTimeoutException.class);
    }

    /**
     * 2. connectionRequestTimeout 재현
     * 풀 사이즈를 1로 잡고, 첫 요청이 커넥션을 오래 붙잡고 있게 만든 뒤
     * 두 번째 요청이 풀에서 커넥션을 못 받고 타임아웃 나는지 확인.
     */
    @Test
    void connectionRequestTimeout_예외_확인() throws Exception {
        int port = startSlowResponseServer(5); // 5초 뒤에야 응답 주는 서버

        RestClient client = buildClient("http://localhost:" + port, 3, 1, 10, 1, 1);

        // 첫 요청이 커넥션을 점유하는 동안(5초짜리 응답 대기)
        CompletableFuture<Void> firstRequest = CompletableFuture.runAsync(() ->
            client.get().retrieve().toBodilessEntity(), executor
        );

        Thread.sleep(500); // 첫 요청이 커넥션을 확실히 점유하도록 대기

        // 두 번째 요청은 풀에 남은 커넥션이 없어 connectionRequestTimeout(1초) 초과
        assertThatThrownBy(() ->
            client.get().retrieve().toBodilessEntity()
        )
        .isInstanceOf(ResourceAccessException.class)
        .cause()
        .isInstanceOf(ConnectionRequestTimeoutException.class);

        firstRequest.get(10, TimeUnit.SECONDS); // 정리
    }

    /**
     * 3. responseTimeout 재현
     * 연결은 받아주지만 응답 바디를 영원히 안 주는 서버를 만듦.
     */
    @Test
    void responseTimeout_예외_확인() throws Exception {
        int port = startHangingServer(); // 연결만 받고 응답 없음

        RestClient client = buildClient("http://localhost:" + port, 3, 3, 2, 10, 10);

        assertThatThrownBy(() ->
            client.get().retrieve().toBodilessEntity()
        )
        .isInstanceOf(ResourceAccessException.class)
        .cause()
        .isInstanceOf(SocketTimeoutException.class);
    }

    @Test
    void noHttpResponse_재현_시도() throws Exception {
        int port = startAcceptThenCloseServer(); // 연결 받고 바로 강제 종료

        RestClient client = buildClient("http://localhost:" + port, 3, 3, 5, 10, 10);

        assertThatThrownBy(() -> client.get().retrieve().toBodilessEntity())
            .isInstanceOf(ResourceAccessException.class)
            .cause()
            .isInstanceOf(SocketException.class);
        // cause는 NoHttpResponseException 또는 SocketException(Connection reset) 둘 다 나올 수 있음
        // — 서버가 얼마나 "조용히" 끊었는지에 따라 갈림
    }

    private int startAcceptThenCloseServer() throws IOException {
        ServerSocket ss = new ServerSocket(0);
        this.serverSocket = ss;
        int port = ss.getLocalPort();
        executor.submit(() -> {
            while (!ss.isClosed()) {
                try (Socket socket = ss.accept()) {
                    socket.setSoLinger(true, 0); // RST로 강제 종료 (FIN 없이)
                } catch (Exception ignored) {}
            }
        });
        return port;
    }

    // ---- 테스트용 서버 유틸 ----

    /** 연결은 즉시 받아주되, N초 후에야 최소 응답을 보내는 서버 (풀 점유 시간 확보용) */
    private int startSlowResponseServer(int delaySeconds) throws IOException {
        serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        executor.submit(() -> {
            while (!serverSocket.isClosed()) {
                try (Socket socket = serverSocket.accept()) {
                    Thread.sleep(delaySeconds * 1000L);
                    OutputStream out = socket.getOutputStream();
                    out.write("HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes());
                    out.flush();
                } catch (Exception ignored) {}
            }
        });
        return port;
    }

    /** 연결만 받고 아무 응답도 주지 않는 서버 (responseTimeout 유도용) */
    private int startHangingServer() throws IOException {
        serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        executor.submit(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    // 응답을 아예 안 보내고 소켓만 열어둠
                } catch (Exception ignored) {}
            }
        });
        return port;
    }
}