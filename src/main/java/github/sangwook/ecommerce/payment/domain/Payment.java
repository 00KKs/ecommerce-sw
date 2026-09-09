package github.sangwook.ecommerce.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;

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

    protected Payment() {
    }

    public Payment(Long orderId, int amount, String paymentKey, UUID idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.paymentStatus = PaymentStatus.READY;
        this.idempotencyKey = idempotencyKey;
    }

    public void approve() {
        if (this.paymentStatus != PaymentStatus.READY) throw new IllegalStateException("이미 처리되었거나 대기 중이 아닌 결제는 승인할 수 없습니다.");
        this.paymentStatus = PaymentStatus.DONE;
    }

    public void aborted() {
        this.paymentStatus = PaymentStatus.ABORTED;
    }
}
