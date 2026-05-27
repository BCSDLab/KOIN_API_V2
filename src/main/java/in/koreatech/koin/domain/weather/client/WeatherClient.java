package in.koreatech.koin.domain.weather.client;

import static java.net.URLEncoder.encode;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

    public WeatherClient(
        @Value("${OPEN_API_KEY_PUBLIC}") String openApiKey,
        RestTemplate restTemplate
    ) {
        this.openApiKey = openApiKey;
        this.restTemplate = restTemplate;
    }

    public WeatherForecast getWeatherForecast(WeatherForecastRequestTime requestTime) {
        WeatherApiResponse response = getOpenApiResponse(requestTime);
        List<WeatherForecastItem> items = extractForecastItems(response);
        Map<String, String> forecasts = items.stream()
            .filter(item -> item.fcstDate().equals(requestTime.forecastDate()))
            .filter(item -> item.fcstTime().equals(requestTime.forecastTime()))
            .collect(Collectors.toMap(
                WeatherForecastItem::category,
                WeatherForecastItem::fcstValue,
                (previous, current) -> current
            ));

        try {
            return new WeatherForecast(
                Integer.parseInt(forecasts.get("TMP")),
                forecasts.get("SKY"),
                forecasts.get("PTY")
            );
        } catch (Exception e) {
            throw WeatherOpenApiException.withDetail("forecastDateTime: "
                + requestTime.forecastDate() + requestTime.forecastTime());
        }
    }

    private WeatherApiResponse getOpenApiResponse(WeatherForecastRequestTime requestTime) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            URL url = new URL(getRequestURL(requestTime));
            ResponseEntity<WeatherApiResponse> response = restTemplate.exchange(
                url.toURI(),
                HttpMethod.GET,
                entity,
                WeatherApiResponse.class
            );
            return response.getBody();
        } catch (WeatherOpenApiException e) {
            throw e;
        } catch (Exception e) {
            throw WeatherOpenApiException.withDetail("baseDateTime: "
                + requestTime.baseDate() + requestTime.baseTime());
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
            throw new KoinIllegalStateException("기상청 단기예보 API URL 생성중 문제가 발생했습니다.", "uri:" + urlBuilder);
        }
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
        if (!resultCode.equals("00") && !resultCode.equals("0")) {
            throw WeatherOpenApiException.withDetail("resultCode: " + resultCode);
        }
        return response.response().body().items().item();
    }
}
