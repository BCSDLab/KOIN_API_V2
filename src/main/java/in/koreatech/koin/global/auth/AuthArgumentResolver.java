package in.koreatech.koin.global.auth;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
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
import in.koreatech.koin.global.auth.exception.AuthorizationException;
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
        Long userId = authContext.getUserId();
        User user = userRepository.getById(userId);

        if (permitStatus.contains(user.getUserType())) {
            if (!user.isAuthed()) {
                if (user.getUserType() == OWNER) {
                    throw new AuthorizationException("관리자 인증 대기중입니다. 지속적으로 해당 문제가 발생한다면 관리자에게 문의주시기바랍니다.");
                }
                if (user.getUserType() == STUDENT) {
                    throw new AuthorizationException("미인증 상태입니다. 아우누리에서 인증메일을 확인해주세요");
                }
                throw AuthorizationException.withDetail("userId: " + user.getId());
            }
            return user.getId();
        }
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        throw AuthorizationException.withDetail("header: " + request);
    }
}
