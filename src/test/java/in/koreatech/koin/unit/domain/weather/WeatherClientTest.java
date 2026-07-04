package in.koreatech.koin.unit.domain.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.weather.client.WeatherClient;
import in.koreatech.koin.domain.weather.client.dto.WeatherForecastRequestTime;
import in.koreatech.koin.domain.weather.exception.WeatherOpenApiException;
import in.koreatech.koin.domain.weather.exception.WeatherOpenApiRateLimitException;
import in.koreatech.koin.domain.weather.model.WeatherForecast;

class WeatherClientTest {

    private static final WeatherForecastRequestTime REQUEST_TIME = new WeatherForecastRequestTime(
        "20240115",
        "1100",
        "20240115",
        "1200"
    );

    private MockRestServiceServer server;
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        RetryTemplate retryTemplate = RetryTemplate.builder()
            .maxAttempts(6)
            .noBackoff()
            .retryOn(WeatherOpenApiRateLimitException.class)
            .build();
        weatherClient = new WeatherClient("test-api-key", restTemplate, new ObjectMapper(), retryTemplate);
    }

    @Test
    void 기상청_예보_응답에서_시간대별_기온_하늘상태_강수형태를_추출한다() {
        server.expect(once(), anything())
            .andRespond(withSuccess(weatherApiResponse("""
                {"category":"TMP","fcstDate":"20240115","fcstTime":"1200","fcstValue":"21"},
                {"category":"SKY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"1"},
                {"category":"PTY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"0"},
                {"category":"TMP","fcstDate":"20240115","fcstTime":"1300","fcstValue":"22"},
                {"category":"SKY","fcstDate":"20240115","fcstTime":"1300","fcstValue":"3"},
                {"category":"PTY","fcstDate":"20240115","fcstTime":"1300","fcstValue":"0"}
                """), MediaType.APPLICATION_JSON));

        Map<String, WeatherForecast> forecasts = weatherClient.getWeatherForecasts(REQUEST_TIME);

        assertThat(forecasts).containsOnlyKeys("202401151200", "202401151300");
        assertThat(forecasts.get("202401151200"))
            .isEqualTo(new WeatherForecast(21, "1", "0"));
        assertThat(forecasts.get("202401151300"))
            .isEqualTo(new WeatherForecast(22, "3", "0"));
        server.verify();
    }

    @Test
    void 갱신_시각부터_향후_24시간의_예보만_추출한다() {
        server.expect(once(), anything())
            .andRespond(withSuccess(weatherApiResponse("""
                {"category":"TMP","fcstDate":"20240115","fcstTime":"1100","fcstValue":"20"},
                {"category":"SKY","fcstDate":"20240115","fcstTime":"1100","fcstValue":"1"},
                {"category":"PTY","fcstDate":"20240115","fcstTime":"1100","fcstValue":"0"},
                {"category":"TMP","fcstDate":"20240115","fcstTime":"1200","fcstValue":"21"},
                {"category":"SKY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"1"},
                {"category":"PTY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"0"},
                {"category":"TMP","fcstDate":"20240116","fcstTime":"1100","fcstValue":"19"},
                {"category":"SKY","fcstDate":"20240116","fcstTime":"1100","fcstValue":"3"},
                {"category":"PTY","fcstDate":"20240116","fcstTime":"1100","fcstValue":"0"},
                {"category":"TMP","fcstDate":"20240116","fcstTime":"1200","fcstValue":"18"},
                {"category":"SKY","fcstDate":"20240116","fcstTime":"1200","fcstValue":"4"},
                {"category":"PTY","fcstDate":"20240116","fcstTime":"1200","fcstValue":"1"}
                """), MediaType.APPLICATION_JSON));

        Map<String, WeatherForecast> forecasts = weatherClient.getWeatherForecasts(REQUEST_TIME);

        assertThat(forecasts).containsOnlyKeys("202401151200", "202401161100");
        server.verify();
    }

    @Test
    void 요청한_예보시각의_데이터가_없으면_직전_발표시각으로_다시_조회한다() {
        server.expect(once(), anything())
            .andRespond(withSuccess(weatherApiResponse("""
                {"category":"TMP","fcstDate":"20240115","fcstTime":"1300","fcstValue":"21"},
                {"category":"SKY","fcstDate":"20240115","fcstTime":"1300","fcstValue":"1"},
                {"category":"PTY","fcstDate":"20240115","fcstTime":"1300","fcstValue":"0"}
                """), MediaType.APPLICATION_JSON));
        server.expect(once(), anything())
            .andRespond(withSuccess(weatherApiResponse("""
                {"category":"TMP","fcstDate":"20240115","fcstTime":"1200","fcstValue":"20"},
                {"category":"SKY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"3"},
                {"category":"PTY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"0"}
                """), MediaType.APPLICATION_JSON));

        Map<String, WeatherForecast> forecasts = weatherClient.getWeatherForecasts(REQUEST_TIME);

        assertThat(forecasts.get("202401151200"))
            .isEqualTo(new WeatherForecast(20, "3", "0"));
        server.verify();
    }

    @Test
    void 필수_예보_항목이_누락되면_비정상_응답으로_처리한다() {
        server.expect(once(), anything())
            .andRespond(withSuccess(weatherApiResponse("""
                {"category":"TMP","fcstDate":"20240115","fcstTime":"1200","fcstValue":"21"},
                {"category":"PTY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"0"}
                """), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> weatherClient.getWeatherForecasts(REQUEST_TIME))
            .isInstanceOf(WeatherOpenApiException.class)
            .hasMessage("기상청 단기예보 API 응답이 정상적이지 않습니다.")
            .satisfies(exception -> assertThat(((WeatherOpenApiException) exception).getFullMessage())
                .contains("missing category: SKY"));

        server.verify();
    }

    @Test
    void 동시_호출_제한이_발생하면_5회까지_재시도한다() {
        server.expect(times(5), anything())
            .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                .body("API rate limit exceeded"));
        server.expect(once(), anything())
            .andRespond(withSuccess(weatherApiResponse("""
                {"category":"TMP","fcstDate":"20240115","fcstTime":"1200","fcstValue":"21"},
                {"category":"SKY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"1"},
                {"category":"PTY","fcstDate":"20240115","fcstTime":"1200","fcstValue":"0"}
                """), MediaType.APPLICATION_JSON));

        Map<String, WeatherForecast> forecasts = weatherClient.getWeatherForecasts(REQUEST_TIME);

        assertThat(forecasts.get("202401151200"))
            .isEqualTo(new WeatherForecast(21, "1", "0"));
        server.verify();
    }

    @Test
    void 동시_호출_제한이_6번_연속_발생하면_최종_예외를_반환한다() {
        server.expect(times(6), anything())
            .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                .body("API rate limit exceeded"));

        assertThatThrownBy(() -> weatherClient.getWeatherForecasts(REQUEST_TIME))
            .isInstanceOf(WeatherOpenApiRateLimitException.class);

        server.verify();
    }

    private String weatherApiResponse(String items) {
        return """
            {
              "response": {
                "header": {
                  "resultCode": "00",
                  "resultMsg": "NORMAL_SERVICE"
                },
                "body": {
                  "items": {
                    "item": [
                      %s
                    ]
                  }
                }
              }
            }
            """.formatted(items);
    }
}
