package github.sangwook.ecommerce.payment.infrastructure;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentGatewayClientConfig {

    @Bean(name = "PaymentGatewayRestClientBuilder")
    public RestClient.Builder restClient() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(3)) //TCP handshake 수립 시간 timeout
            .setValidateAfterInactivity(Timeout.ofSeconds(10)) //커넥션이 10초 이상 idle 상태였다면, 재사용 직전에 살아있는지 검증 후 사용 (죽어있으면 새로 생성한다)
            .build();

        //Basic과 다르게 Pooling은 동시 커넥션 수를 여러개 관리 가능하다, Basic은 단 하나의 요청만 처리 가능
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnTotal(30) //최대 커넥션 값 (보통 API 요청 TPS를 보고 조정한다)
            .setMaxConnPerRoute(30) //라우트 별 최대 커넥션 (외부 API를 여러개 사용 시 조정한다)
            //TODO 커넥션 설정은 추후 성능 테스트 시 조정 가능
            .setDefaultConnectionConfig(connectionConfig)
            .build();

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(3)) //풀에서 커넥션 획득 대기 시간
            .setResponseTimeout(Timeout.ofSeconds(5)) //요청을 모두 보낸 후, 서버로부터의 응답이 오기까지의 시간
            .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(poolingHttpClientConnectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
            .requestFactory(factory)
            .baseUrl("http://pg:9090");
    }

}
