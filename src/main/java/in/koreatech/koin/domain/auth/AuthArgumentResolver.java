package in.koreatech.koin.domain.auth;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import in.koreatech.koin.domain.auth.exception.AuthException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import static java.util.Objects.requireNonNull;
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
        requireNonNull(authAt);
        List<UserType> permitStatus = Arrays.asList(authAt.permit());
        Long userId = authContext.getUserId();
        if (isAnonymous(userId, authAt)) {
            return null;
        }
        User user = userRepository.getById(userId);
        if (permitStatus.contains(user.getUserType())) {
            return user.getId();
        }
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        throw AuthException.withDetail("header: " + request);
    }

    private static boolean isAnonymous(Long userId, Auth authAt) {
        if (userId == null) {
            if (authAt.anonymous()) {
                return true;
            }
            throw AuthException.withDetail("userId is null");
        }
        return false;
    }
}
