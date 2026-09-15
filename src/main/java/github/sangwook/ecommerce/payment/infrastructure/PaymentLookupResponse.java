package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.PaymentStatus;
import java.time.OffsetDateTime;

public record PaymentLookupResponse(
        String paymentKey,
        String orderId,
        PaymentStatus status,
        Integer amount,
        OffsetDateTime approvedAt,
        OffsetDateTime canceledAt
) {}