package in.koreatech.koin.admin.abtest.useragent;

import java.io.IOException;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import ua_parser.Client;
import ua_parser.Parser;

@Component
public class UserAgentArgumentResolver implements HandlerMethodArgumentResolver {

    private final Parser uaParser = new Parser();

    public UserAgentArgumentResolver() throws IOException {
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserAgent.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String userAgent = webRequest.getHeader("User-Agent");
        if (userAgent == null) {
            return null;
        }

        Client client = uaParser.parse(userAgent);
        String type = determineDeviceType(userAgent);
        return UserAgentInfo.builder()
            .model(client.device.family)
            .type(type)
            .build();
    }


    private String determineDeviceType(String userAgent) {
        // 태블릿 기기를 나타내는 패턴 검사
        String[] tabletIndicators = {"Tablet", "iPad"};
        for (String indicator : tabletIndicators) {
            if (userAgent.toLowerCase().contains(indicator.toLowerCase())) {
                return "Tablet";
            }
        }

        // 모바일 기기를 나타내는 패턴 검사
        String[] mobileIndicators = {"Mobile", "Mobi", "Android", "iPhone", "Windows Phone"};
        for (String indicator : mobileIndicators) {
            if (userAgent.toLowerCase().contains(indicator.toLowerCase())) {
                return "Mobile";
            }
        }

        // 모바일과 태블릿 모두 해당하지 않으면 PC로 간주
        return "PC";
    }
}
