package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import in.koreatech.koin.domain.weather.model.WeatherCache;
import in.koreatech.koin.domain.weather.repository.WeatherCacheRepository;

class WeatherApiTest extends AcceptanceTest {

    @Autowired
    private WeatherCacheRepository weatherCacheRepository;

    @Test
    void 병천_날씨를_조회한다() throws Exception {
        clear();
        weatherCacheRepository.save(WeatherCache.of(
            new WeatherResponse(21, "맑음")
        ));

        mockMvc.perform(
                get("/weather")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "temperature": 21,
                    "weather": "맑음",
                    "weather_id": 1,
                    "weather_icon_url": "https://static.koreatech.in/upload/WEATHER/sun-03.webp"
                }
                """));
    }
}
