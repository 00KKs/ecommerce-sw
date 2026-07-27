package github.sangwook.ecommerce.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public @Nullable Object resolveArgument(
        @NonNull MethodParameter parameter,
        @Nullable ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) return null;
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnAuthorizedException();
        }
        Object loginMember = session.getAttribute(SessionKeys.LOGIN_MEMBER);
        if (loginMember == null) {
            throw new UnAuthorizedException();
        }
        return loginMember;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginMember.class);
        boolean isMemberSessionType = parameter.getParameterType() == MemberSession.class;
        return hasAnnotation && isMemberSessionType;
    }
}
