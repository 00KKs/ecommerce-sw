package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.PaymentGateway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FakePaymentGatewayConfig {

    @Bean
    @Primary
    public PaymentGateway paymentGatewayClient() {
        return new FakePaymentGateway();
    }

}
