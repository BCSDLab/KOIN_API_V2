package in.koreatech.koin.domain.dining.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DiningReportBotInterceptor implements HandlerInterceptor {

    private static final String SERVICE_TOKEN_HEADER = "X-Koin-Service-Token";

    private final String botToken;

    public DiningReportBotInterceptor(@Value("${dining.report.bot-token:}") String botToken) {
        this.botToken = botToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String serviceToken = request.getHeader(SERVICE_TOKEN_HEADER);
        if (!StringUtils.hasText(botToken) || !StringUtils.hasText(serviceToken)
            || !MessageDigest.isEqual(botToken.getBytes(UTF_8), serviceToken.getBytes(UTF_8))) {
            throw CustomException.of(ApiResponseCode.BOT_AUTHENTICATION_FAILED);
        }
        return true;
    }
}
