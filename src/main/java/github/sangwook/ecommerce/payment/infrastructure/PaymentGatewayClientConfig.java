package github.sangwook.ecommerce.payment.infrastructure;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentGatewayClientConfig {

    @Bean(name = "PaymentGatewayRestClientBuilder")
    public RestClient.Builder restClient() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
            .requestFactory(factory)
            .baseUrl("http://pg:9090");
    }

}
