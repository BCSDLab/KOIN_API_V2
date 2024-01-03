package in.koreatech.koin.domain.auth.resolver;

import in.koreatech.koin.domain.auth.JwtProvider;
import in.koreatech.koin.domain.auth.StudentAuth;
import in.koreatech.koin.domain.auth.exception.AuthException;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.repository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class StudentArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;
    private final StudentRepository studentRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(StudentAuth.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (nativeRequest == null) {
            throw new AuthException("요청 값이 비어있습니다.");
        }
        String request = nativeRequest.getHeader("Authorization");
        if (request == null) {
            throw new AuthException("인증 헤더값이 비어있습니다. request: " + nativeRequest);
        }
        Long userId = jwtProvider.getUserId(request);
        return studentRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.witDetail("request: " + request));
    }
}
