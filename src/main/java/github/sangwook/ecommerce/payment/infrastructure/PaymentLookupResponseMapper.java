package github.sangwook.ecommerce.payment.infrastructure;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
final class PaymentLookupResponseMapper {

    private PaymentLookupResponseMapper() {}

    static PaymentLookupResult toResult(@Nonnull PaymentLookupResponse response) {
        return switch (response.status()) {
            case READY -> {
                validateNoTimestamps(response);
                yield new PaymentLookupResult.READY();
            }
            case DONE -> {
                validateApprovedOnly(response);
                yield new PaymentLookupResult.DONE(response.amount(), response.approvedAt());
            }
            case CANCELED -> {
                validateBothTimestamps(response);
                yield new PaymentLookupResult.CANCELED(
                    response.amount(), response.approvedAt(), response.canceledAt());
            }
            case ABORTED -> {
                validateNoTimestamps(response);
                yield new PaymentLookupResult.ABORTED();
            }
        };
    }

    private static void validateNoTimestamps(PaymentLookupResponse response) {
        if (response.approvedAt() != null || response.canceledAt() != null) {
            log.warn("PG 응답 불일치: status={}, timestamp 존재. paymentKey={}", response.status(), response.paymentKey());
        }
    }

    private static void validateApprovedOnly(PaymentLookupResponse response) {
        if (response.approvedAt() == null) {
            log.warn("PG 응답 불일치: status=DONE, approvedAt 누락. paymentKey={}", response.paymentKey());
        }
        if (response.canceledAt() != null) {
            log.warn("PG 응답 불일치: status=DONE, canceledAt 존재. paymentKey={}", response.paymentKey());
        }
    }

    private static void validateBothTimestamps(PaymentLookupResponse r) {
        if (r.approvedAt() == null || r.canceledAt() == null) {
            log.warn("PG 응답 불일치: status=CANCELED, timestamp 누락. paymentKey={}", r.paymentKey());
        }
    }
}