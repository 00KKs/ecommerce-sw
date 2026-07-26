package github.sangwook.ecommerce.member.api;

import github.sangwook.ecommerce.auth.LoginMember;
import github.sangwook.ecommerce.auth.MemberSession;
import github.sangwook.ecommerce.member.api.dto.AddressCreateRequest;
import github.sangwook.ecommerce.member.application.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<Void> create(@LoginMember MemberSession memberSession, @RequestBody AddressCreateRequest request) {
        addressService.create(
            memberSession.getId(),
            request.getRecipientName(),
            request.getRecipientPhone(),
            request.getAddress(),
            request.getDeliveryRequest(),
            request.isDefault()
        );
        return ResponseEntity.ok().build();
    }

}
