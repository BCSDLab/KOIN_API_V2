package in.koreatech.koin.infrastructure.upstage.client;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "upstage")
public class UpstageProperties {

    private String apiKey = "";
    private String apiBaseUrl = "https://api.upstage.ai/v1";

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(apiKey)) {
            return;
        }
        URI uri = URI.create(apiBaseUrl);
        if (!"https".equals(uri.getScheme()) || !"api.upstage.ai".equals(uri.getHost())) {
            throw new KoinIllegalStateException("Upstage API base URL 설정이 올바르지 않습니다.");
        }
    }
}
