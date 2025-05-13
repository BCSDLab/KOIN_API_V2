package in.koreatech.koin._common.auth;

import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
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
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        Auth authAt = parameter.getParameterAnnotation(Auth.class);
        Objects.requireNonNull(authAt);

        if (authContext.isAnonymous() && authAt.anonymous()) {
            return null;
        }

        Integer userId = authContext.getUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> AuthenticationException.withDetail("이미 탈퇴한 계정입니다. userId: " + userId));

        return user.authorizeAndGetId(authAt.permit());
    }
}
