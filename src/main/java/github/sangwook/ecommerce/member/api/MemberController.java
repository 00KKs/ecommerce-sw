package github.sangwook.ecommerce.member.api;

import github.sangwook.ecommerce.auth.MemberSession;
import github.sangwook.ecommerce.auth.SessionKeys;
import github.sangwook.ecommerce.member.api.dto.MemberJoinRequest;
import github.sangwook.ecommerce.member.api.dto.MemberLoginRequest;
import github.sangwook.ecommerce.member.application.MemberService;
import github.sangwook.ecommerce.openapi.MemberOpenApiDocs;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController implements MemberOpenApiDocs {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestBody MemberJoinRequest request) {
        memberService.join(request.getEmail(), request.getPassword(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody MemberLoginRequest request, HttpSession session) {
        MemberSession memberSession = memberService.login(request.getEmail(), request.getPassword());
        session.setAttribute(SessionKeys.LOGIN_MEMBER, memberSession);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }
}
