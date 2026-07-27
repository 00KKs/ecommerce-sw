package github.sangwook.ecommerce.member.api;

import github.sangwook.ecommerce.auth.LoginMember;
import github.sangwook.ecommerce.auth.MemberSession;
import github.sangwook.ecommerce.member.api.dto.AddressCreateRequest;
import github.sangwook.ecommerce.member.api.dto.AddressDeleteRequest;
import github.sangwook.ecommerce.member.api.dto.AddressResponse;
import github.sangwook.ecommerce.member.api.dto.AddressUpdateDefaultRequest;
import github.sangwook.ecommerce.member.api.dto.AddressUpdateRequest;
import github.sangwook.ecommerce.member.application.AddressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/update")
    public ResponseEntity<Void> update(@LoginMember MemberSession memberSession, @RequestBody AddressUpdateRequest request) {
        addressService.update(
            memberSession.getId(),
            request.getAddressId(),
            request.getRecipientName(),
            request.getRecipientPhone(),
            request.getAddress(),
            request.getDeliveryRequest()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getList(@LoginMember MemberSession memberSession) {
        return ResponseEntity.ok(addressService.getList(memberSession.getId()));
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@LoginMember MemberSession memberSession, @RequestBody AddressDeleteRequest request) {
        addressService.delete(memberSession.getId(), request.getAddressId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/default")
    public ResponseEntity<Void> updateDefault(@LoginMember MemberSession memberSession, @RequestBody AddressUpdateDefaultRequest request) {
        addressService.changeDefault(memberSession.getId(), request.getAddressId());
        return ResponseEntity.ok().build();
    }

}
