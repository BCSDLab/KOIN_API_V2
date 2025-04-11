package in.koreatech.koin._common.auth;

import static java.util.Objects.requireNonNull;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmsAuthedArgumentResolver implements HandlerMethodArgumentResolver {

    private final SmsAuthedContext smsAuthedContext;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SmsAuthed.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        SmsAuthed smsAuthed = parameter.getParameterAnnotation(SmsAuthed.class);
        requireNonNull(smsAuthed);
        return smsAuthedContext.getPhoneNumberAuthed();
    }
}
