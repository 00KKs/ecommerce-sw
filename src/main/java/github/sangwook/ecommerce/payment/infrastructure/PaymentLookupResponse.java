package github.sangwook.ecommerce.payment.infrastructure;

import java.time.OffsetDateTime;

public record PaymentLookupResponse(
        String paymentKey,
        String orderId,
        PaymentGatewayPaymentStatus status,
        Integer amount,
        OffsetDateTime approvedAt,
        OffsetDateTime canceledAt
) {}