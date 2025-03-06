package in.koreatech.koin.integration.kakao.config;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

@RestControllerAdvice
public class KakaoRequestBodyAdvice implements RequestBodyAdvice {

    private String requestBody;

    @Override
    public boolean supports(
        MethodParameter methodParameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return methodParameter.hasParameterAnnotation(KakaoRequest.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(
        HttpInputMessage inputMessage,
        MethodParameter parameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputMessage.getBody(), StandardCharsets.UTF_8)
        )) {
            requestBody = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception ignored) {
        }
        ByteArrayInputStream newInputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return newInputStream;
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }

    @Override
    public Object afterBodyRead(
        Object body,
        HttpInputMessage inputMessage,
        MethodParameter parameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        KakaoRequest authAt = parameter.getParameterAnnotation(KakaoRequest.class);
        requireNonNull(authAt, "KakaoRequest annotation cannot be null");
        return authAt.type().parse(requestBody);
    }

    @Override
    public Object handleEmptyBody(
        Object body,
        HttpInputMessage inputMessage,
        MethodParameter parameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return body;
    }
}
