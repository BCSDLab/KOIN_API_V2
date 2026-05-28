package in.koreatech.koin.unit.domain.weather;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.weather.dto.WeatherResponse;

class WeatherResponseTest {

    @Test
    void 날씨_id와_아이콘_url_중_하나라도_없으면_날씨_상태_기준으로_함께_보정한다() {
        WeatherResponse response = new WeatherResponse(21, "맑음", 3, null);

        assertThat(response.weatherId()).isEqualTo(1);
        assertThat(response.weatherIconUrl()).isEqualTo("https://static.koreatech.in/weather/sunny.png");
    }
}
