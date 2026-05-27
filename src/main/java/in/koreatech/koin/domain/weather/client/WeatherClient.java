package in.koreatech.koin.domain.weather.client;

import static java.net.URLEncoder.encode;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.weather.dto.WeatherApiResponse;
import in.koreatech.koin.domain.weather.dto.WeatherApiResponse.WeatherForecastItem;
import in.koreatech.koin.domain.weather.exception.WeatherOpenApiException;
import in.koreatech.koin.domain.weather.model.WeatherForecast;
import in.koreatech.koin.domain.weather.model.WeatherForecastRequestTime;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;

@Component
public class WeatherClient {

    private static final String OPEN_API_URL =
        "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private static final String ENCODE_TYPE = "UTF-8";
    private static final int BYEONGCHEON_NX = 66;
    private static final int BYEONGCHEON_NY = 109;
    private static final int ROW_COUNT = 1000;

    private final String openApiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherClient(
        @Value("${OPEN_API_KEY_PUBLIC}") String openApiKey,
        RestTemplate restTemplate,
        ObjectMapper objectMapper
    ) {
        this.openApiKey = openApiKey;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public WeatherForecast getWeatherForecast(WeatherForecastRequestTime requestTime) {
        try {
            return getWeatherForecastWithoutFallback(requestTime);
        } catch (WeatherOpenApiException e) {
            if (!canRetryWithPreviousBaseTime(e)) {
                throw e;
            }
            return getWeatherForecastWithoutFallback(requestTime.previousBaseTime());
        }
    }

    private WeatherForecast getWeatherForecastWithoutFallback(WeatherForecastRequestTime requestTime) {
        WeatherApiResponse response = getOpenApiResponse(requestTime);
        List<WeatherForecastItem> items = extractForecastItems(response);
        Map<String, String> forecasts = items.stream()
            .filter(item -> requestTime.forecastDate().equals(item.fcstDate()))
            .filter(item -> requestTime.forecastTime().equals(item.fcstTime()))
            .collect(Collectors.toMap(
                WeatherForecastItem::category,
                WeatherForecastItem::fcstValue,
                (previous, current) -> current
            ));

        if (forecasts.isEmpty()) {
            throw WeatherOpenApiException.withDetail("forecastDateTime: "
                + requestTime.forecastDate() + requestTime.forecastTime());
        }

        String temperature = requireForecastValue(forecasts, "TMP");
        String sky = requireForecastValue(forecasts, "SKY");
        String precipitationType = requireForecastValue(forecasts, "PTY");

        try {
            return new WeatherForecast(
                Integer.parseInt(temperature),
                sky,
                precipitationType
            );
        } catch (NumberFormatException e) {
            throw WeatherOpenApiException.withDetail(
                "invalid category: TMP, value: " + temperature
            );
        }
    }

    private String requireForecastValue(Map<String, String> forecasts, String category) {
        String forecastValue = forecasts.get(category);
        if (forecastValue == null || forecastValue.isBlank()) {
            throw WeatherOpenApiException.withDetail(
                "missing category: " + category
            );
        }
        return forecastValue;
    }

    private boolean canRetryWithPreviousBaseTime(WeatherOpenApiException e) {
        String errorMessage = e.getFullMessage();
        return errorMessage.contains("NO_DATA")
            || errorMessage.contains("response body is empty")
            || errorMessage.contains("forecastDateTime");
    }

    private WeatherApiResponse getOpenApiResponse(WeatherForecastRequestTime requestTime) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "*/*");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            URL url = new URL(getRequestURL(requestTime));
            ResponseEntity<String> response = restTemplate.exchange(
                url.toURI(),
                HttpMethod.GET,
                entity,
                String.class
            );
            return parseResponse(response.getBody(), requestTime);
        } catch (WeatherOpenApiException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isBlank()) {
                return parseResponse(responseBody, requestTime);
            }
            throw WeatherOpenApiException.withDetail("baseDateTime: "
                + requestTime.baseDate() + requestTime.baseTime() + ", httpStatus: " + e.getStatusCode());
        } catch (Exception e) {
            throw WeatherOpenApiException.withDetail("baseDateTime: "
                + requestTime.baseDate() + requestTime.baseTime() + ", cause: " + e.getClass().getSimpleName());
        }
    }

    private WeatherApiResponse parseResponse(String responseBody, WeatherForecastRequestTime requestTime) {
        if (responseBody == null || responseBody.isBlank()) {
            throw WeatherOpenApiException.withDetail("baseDateTime: "
                + requestTime.baseDate() + requestTime.baseTime() + ", response body is empty");
        }

        if (!responseBody.trim().startsWith("{")) {
            throw WeatherOpenApiException.withDetail("baseDateTime: "
                + requestTime.baseDate() + requestTime.baseTime() + ", " + extractXmlErrorMessage(responseBody));
        }

        try {
            return objectMapper.readValue(responseBody, WeatherApiResponse.class);
        } catch (Exception e) {
            throw WeatherOpenApiException.withDetail("baseDateTime: "
                + requestTime.baseDate() + requestTime.baseTime() + ", invalid JSON response");
        }
    }

    private String getRequestURL(WeatherForecastRequestTime requestTime) {
        StringBuilder urlBuilder = new StringBuilder(OPEN_API_URL);
        try {
            urlBuilder.append("?" + encode("serviceKey", ENCODE_TYPE) + "=" + encode(openApiKey, ENCODE_TYPE));
            urlBuilder.append("&" + encode("numOfRows", ENCODE_TYPE) + "="
                + encode(String.valueOf(ROW_COUNT), ENCODE_TYPE));
            urlBuilder.append("&" + encode("pageNo", ENCODE_TYPE) + "=" + encode("1", ENCODE_TYPE));
            urlBuilder.append("&" + encode("dataType", ENCODE_TYPE) + "=" + encode("JSON", ENCODE_TYPE));
            urlBuilder.append("&" + encode("base_date", ENCODE_TYPE) + "="
                + encode(requestTime.baseDate(), ENCODE_TYPE));
            urlBuilder.append("&" + encode("base_time", ENCODE_TYPE) + "="
                + encode(requestTime.baseTime(), ENCODE_TYPE));
            urlBuilder.append("&" + encode("nx", ENCODE_TYPE) + "=" + encode(String.valueOf(BYEONGCHEON_NX), ENCODE_TYPE));
            urlBuilder.append("&" + encode("ny", ENCODE_TYPE) + "=" + encode(String.valueOf(BYEONGCHEON_NY), ENCODE_TYPE));
            return urlBuilder.toString();
        } catch (Exception e) {
            throw new KoinIllegalStateException("기상청 단기예보 API URL 생성중 문제가 발생했습니다.", "uri build failed");
        }
    }

    private String extractXmlErrorMessage(String responseBody) {
        String returnAuthMsg = extractTagValue(responseBody, "returnAuthMsg");
        String returnReasonCode = extractTagValue(responseBody, "returnReasonCode");
        if (returnAuthMsg != null || returnReasonCode != null) {
            return "returnAuthMsg: " + returnAuthMsg + ", returnReasonCode: " + returnReasonCode;
        }
        return "non JSON response";
    }

    private String extractTagValue(String responseBody, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        int startIndex = responseBody.indexOf(startTag);
        int endIndex = responseBody.indexOf(endTag);
        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            return null;
        }
        return responseBody.substring(startIndex + startTag.length(), endIndex);
    }

    private List<WeatherForecastItem> extractForecastItems(WeatherApiResponse response) {
        if (response == null
            || response.response() == null
            || response.response().header() == null
            || response.response().body() == null
            || response.response().body().items() == null
            || response.response().body().items().item() == null) {
            throw WeatherOpenApiException.withDetail("response body is empty");
        }

        String resultCode = response.response().header().resultCode();
        if (!"00".equals(resultCode) && !"0".equals(resultCode)) {
            throw WeatherOpenApiException.withDetail(
                "resultCode: " + resultCode + ", resultMsg: " + response.response().header().resultMsg()
            );
        }
        return response.response().body().items().item();
    }
}
