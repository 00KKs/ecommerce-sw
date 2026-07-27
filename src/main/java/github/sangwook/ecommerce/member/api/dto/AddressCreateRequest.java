package github.sangwook.ecommerce.member.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressCreateRequest {

    private String recipientName;
    private String recipientPhone;
    private String address;
    private String deliveryRequest;
    private boolean isDefault;

}
