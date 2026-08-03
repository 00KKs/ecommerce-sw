package github.sangwook.ecommerce.openapi;

import github.sangwook.ecommerce.auth.LoginMember;
import github.sangwook.ecommerce.auth.MemberSession;
import github.sangwook.ecommerce.member.api.dto.AddressCreateRequest;
import github.sangwook.ecommerce.member.api.dto.AddressDeleteRequest;
import github.sangwook.ecommerce.member.api.dto.AddressResponse;
import github.sangwook.ecommerce.member.api.dto.AddressUpdateDefaultRequest;
import github.sangwook.ecommerce.member.api.dto.AddressUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Address", description = "회원 배송지 관리 API")
public interface AddressOpenApiDocs {

    @Operation(
        summary = "배송지 등록",
        description = "로그인한 회원의 새 배송지를 등록합니다. isDefault가 true면 기본 배송지로 설정됩니다."
    )
    @ApiResponse(responseCode = "200", description = "등록 성공")
    ResponseEntity<Void> create(@LoginMember MemberSession memberSession, AddressCreateRequest request);

    @Operation(
        summary = "배송지 수정",
        description = "기존 배송지의 수령인·연락처·주소·배송 요청사항을 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    ResponseEntity<Void> update(@LoginMember MemberSession memberSession, AddressUpdateRequest request);

    @Operation(
        summary = "배송지 목록 조회",
        description = "로그인한 회원의 전체 배송지 목록을 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<List<AddressResponse>> getList(@LoginMember MemberSession memberSession);

    @Operation(
        summary = "배송지 삭제",
        description = "지정한 배송지를 삭제합니다."
    )
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    ResponseEntity<Void> delete(@LoginMember MemberSession memberSession, AddressDeleteRequest request);

    @Operation(
        summary = "기본 배송지 변경",
        description = "지정한 배송지를 기본 배송지로 설정합니다. 기존 기본 배송지는 해제됩니다."
    )
    @ApiResponse(responseCode = "200", description = "변경 성공")
    ResponseEntity<Void> updateDefault(@LoginMember MemberSession memberSession, AddressUpdateDefaultRequest request);
}