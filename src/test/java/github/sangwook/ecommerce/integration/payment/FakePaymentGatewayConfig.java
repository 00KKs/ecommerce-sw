package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.application.PaymentGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FakePaymentGatewayConfig {

    @Bean
    @Primary
    public PaymentGateway fakePaymentGateway(
        @Value("${fake.payment.initiate:SUCCESS}") String initiateScenario,
        @Value("${fake.payment.confirm:SUCCESS}") String confirmScenario) {
        return new ComposableFakePaymentGateway(
            InitiateScenario.valueOf(initiateScenario),
            ConfirmScenario.valueOf(confirmScenario)
        );
    }
}
