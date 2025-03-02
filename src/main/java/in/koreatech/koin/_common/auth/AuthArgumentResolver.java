package in.koreatech.koin._common.auth;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;

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
        requireNonNull(authAt);
        List<UserType> permitStatus = Arrays.asList(authAt.permit());
        if (authContext.isAnonymous() && authAt.anonymous()) {
            return null;
        }
        Integer userId = authContext.getUserId();
        User user = userRepository.getById(userId);

        if (permitStatus.contains(user.getUserType())) {
            if (!user.isAuthed()) {
                if (user.getUserType() == OWNER) {
                    throw new AuthorizationException("관리자 인증 대기중입니다.");
                }
                if (user.getUserType() == STUDENT) {
                    throw new AuthorizationException("미인증 상태입니다. 아우누리에서 인증메일을 확인해주세요");
                }
                if (user.getUserType() == ADMIN) {
                    throw new AuthorizationException("PL 인증 대기중입니다.");
                }
                throw AuthorizationException.withDetail("userId: " + user.getId());
            }
            return user.getId();
        }
        HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
        throw AuthorizationException.withDetail("header: " + request);
    }
}
