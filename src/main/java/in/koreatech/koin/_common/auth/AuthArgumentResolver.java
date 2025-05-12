package in.koreatech.koin._common.auth;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;
    private final AuthContext authContext;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Auth authAt = parameter.getParameterAnnotation(Auth.class);
        Objects.requireNonNull(authAt);

        List<UserType> permitStatus = Arrays.asList(authAt.permit());
        if (authContext.isAnonymous() && authAt.anonymous()) {
            return null;
        }
        Integer userId = authContext.getUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> AuthorizationException.withDetail("이미 탈퇴한 계정입니다. userId: " + userId));

        if (user.isDeleted()) {
            throw AuthorizationException.withDetail("이미 탈퇴한 계정입니다. userId: " + user.getId());
        }

        if (permitStatus.contains(user.getUserType())) {
            if (!user.isAuthed()) {
                if (user.getUserType() == OWNER) {
                    throw  AuthorizationException.withDetail("관리자 인증 대기중입니다. userId: " + user.getId());
                }
                if (user.getUserType() == STUDENT) {
                    throw AuthorizationException.withDetail("아우누리에서 인증메일을 확인해주세요. userId: " + user.getId());
                }
                if (user.getUserType() == ADMIN) {
                    throw AuthorizationException.withDetail("PL 인증 대기중입니다. userId: " + user.getId());
                }
                throw AuthorizationException.withDetail("유효화지 않은 유저 타입 입니다. userId: " + user.getId());
            }
            return user.getId();
        }
        HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
        throw AuthorizationException.withDetail("header: " + request);
    }
}
