package in.koreatech.koin.acceptance.domain;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.weather.client.WeatherClient;
import in.koreatech.koin.domain.weather.model.WeatherForecast;
import in.koreatech.koin.domain.weather.model.WeatherForecastRequestTime;

class WeatherApiTest extends AcceptanceTest {

    @MockBean
    private WeatherClient weatherClient;

    @Test
    void 병천_날씨를_조회한다() throws Exception {
        clear();
        when(weatherClient.getWeatherForecast(new WeatherForecastRequestTime("20240115", "1100", "20240115", "1200")))
            .thenReturn(new WeatherForecast(21, "1", "0"));

        mockMvc.perform(
                get("/weather")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "temperature": 21,
                    "weather": "맑음"
                }
                """));
    }
}
