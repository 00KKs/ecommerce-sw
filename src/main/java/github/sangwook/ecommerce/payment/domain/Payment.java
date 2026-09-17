package github.sangwook.ecommerce.payment.domain;

import github.sangwook.ecommerce.payment.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private UUID idempotencyKey;

    @Column(name = "check_count")
    private int checkCount;

    @Setter
    @Column(name = "next_check_at")
    private OffsetDateTime nextCheckAt;

    protected Payment() {
    }

    public Payment(Long orderId, int amount, String paymentKey, UUID idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.paymentStatus = PaymentStatus.READY;
        this.idempotencyKey = idempotencyKey;
        this.checkCount = 0;
        this.nextCheckAt = null;
    }

    public void approve() {
        this.paymentStatus = PaymentStatus.DONE;
    }

    public void aborted() {
        this.paymentStatus = PaymentStatus.ABORTED;
    }

    public void markAsUnknown() {
        this.paymentStatus = PaymentStatus.UNKNOWN;
    }

    public boolean hasExceededRetryLimit(int maxRetryCount) {
        return this.checkCount >= maxRetryCount;
    }

    public void marksAsRequiresReview() {
        this.paymentStatus = PaymentStatus.MANUAL_REVIEW_REQUIRED;
    }

    public void scheduleNextCheck(Duration delay) {
        this.checkCount++;
        this.nextCheckAt = OffsetDateTime.now().plus(delay);
    }
}
