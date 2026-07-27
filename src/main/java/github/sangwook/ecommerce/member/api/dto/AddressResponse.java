package github.sangwook.ecommerce.member.api.dto;

import lombok.Getter;

@Getter
public class AddressResponse {

    private final Long addressId;
    private final String recipientName;
    private final String recipientPhone;
    private final String address;
    private final String deliveryRequest;
    private final boolean isDefault;

    public AddressResponse(Long addressId, String recipientName, String recipientPhone, String address, String deliveryRequest, boolean isDefault) {
        this.addressId = addressId;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.address = address;
        this.deliveryRequest = deliveryRequest;
        this.isDefault = isDefault;
    }
}
