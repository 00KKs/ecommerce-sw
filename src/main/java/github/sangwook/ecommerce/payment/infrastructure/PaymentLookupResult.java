package github.sangwook.ecommerce.payment.infrastructure;

import java.time.OffsetDateTime;

public sealed interface PaymentLookupResult {
    record READY() implements PaymentLookupResult {}
    record DONE(int amount, OffsetDateTime approvedAt) implements PaymentLookupResult {}
    record CANCELED(int amount, OffsetDateTime approvedAt, OffsetDateTime canceledAt) implements PaymentLookupResult {}
    record ABORTED() implements PaymentLookupResult {}
    record NOT_FOUND() implements PaymentLookupResult {}
}
