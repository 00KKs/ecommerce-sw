package github.sangwook.ecommerce.payment.port;

public interface OrderPort {
    void confirmOrder(Long orderId);
    void failOrder(Long orderId);
}
