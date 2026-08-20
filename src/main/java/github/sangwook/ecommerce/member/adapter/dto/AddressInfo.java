package github.sangwook.ecommerce.member.adapter.dto;

import lombok.Getter;

@Getter
public class AddressInfo {
    private final String recipientName;
    private final String recipientPhone;
    private final String address;
    private final String deliveryRequest;

    public AddressInfo(String recipientName, String recipientPhone, String address, String deliveryRequest) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.address = address;
        this.deliveryRequest = deliveryRequest;
    }
}
