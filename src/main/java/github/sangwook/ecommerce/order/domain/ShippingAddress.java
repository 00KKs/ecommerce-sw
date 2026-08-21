package github.sangwook.ecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ShippingAddress {

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "address")
    private String address;

    @Column(name = "delivery_request")
    private String deliveryRequest;

    protected ShippingAddress() {
    }

    public ShippingAddress(String recipientName, String recipientPhone, String address, String deliveryRequest) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.address = address;
        this.deliveryRequest = deliveryRequest;
    }
}
