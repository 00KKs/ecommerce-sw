package github.sangwook.ecommerce.openapi;

import github.sangwook.ecommerce.member.api.dto.MemberJoinRequest;
import github.sangwook.ecommerce.member.api.dto.MemberLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;

@Tag(name = "Member", description = "회원 가입·로그인·로그아웃 API")
public interface MemberOpenApiDocs {

    @Operation(
        summary = "회원 가입",
        description = "이메일, 비밀번호, 이름으로 신규 회원을 등록합니다."
    )
    @ApiResponse(responseCode = "201", description = "가입 성공")
    ResponseEntity<Void> join(MemberJoinRequest request);

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 인증하고 세션을 생성합니다. "
            + "성공 시 JSESSIONID 쿠키가 발급됩니다."
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    ResponseEntity<Void> login(MemberLoginRequest request, HttpSession session);

    @Operation(
        summary = "로그아웃",
        description = "현재 세션을 무효화하고 JSESSIONID 쿠키를 제거합니다."
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response);
}