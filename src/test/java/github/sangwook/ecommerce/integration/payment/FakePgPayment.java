package github.sangwook.ecommerce.integration.payment;

import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class FakePgPayment {
    private final String paymentKey;
    private final Long orderId;
    private final int amount;
    private final List<UUID> confirmIdempotencyKeys = new CopyOnWriteArrayList<>();

    public FakePgPayment(String paymentKey, Long orderId, int amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
    }

    int recordConfirm(UUID idempotencyKey) {
        confirmIdempotencyKeys.add(idempotencyKey);
        return confirmIdempotencyKeys.size();
    }
}
