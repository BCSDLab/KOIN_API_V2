package in.koreatech.koin.domain.bus.city.util;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.global.exception.MalformedApiUriException;

@Component
public class URIProvider {

    private static final String CHEONAN_CITY_CODE = "34010";

    private final String openApiKey;

    public URIProvider(@Value("${OPEN_API_KEY_PUBLIC}") String openApiKey) {
        this.openApiKey = openApiKey;
    }

    public URI getRequestURI(String requestUrl, String nodeId, String numOfRows) {
        String uriString = MessageFormat.format(
            "{0}?serviceKey={1}&numOfRows={2}&cityCode={3}&nodeId={4}&_type=json",
            requestUrl,
            encode(openApiKey, UTF_8),
            encode(numOfRows, UTF_8),
            encode(CHEONAN_CITY_CODE, UTF_8),
            encode(nodeId, UTF_8)
        );
        try {
            return new URI(uriString);
        } catch (Exception e) {
            throw MalformedApiUriException.withDetail("uri: " + uriString);
        }
    }
}
