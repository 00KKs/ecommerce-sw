package github.sangwook.ecommerce.order.domain;

import lombok.Getter;

@Getter
public class AddressSnapshot {
    private final String recipientName;
    private final String recipientPhone;
    private final String address;
    private final String deliveryRequest;

    public AddressSnapshot(String recipientName, String recipientPhone, String address, String deliveryRequest) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.address = address;
        this.deliveryRequest = deliveryRequest;
    }
}
