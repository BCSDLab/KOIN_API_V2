package in.koreatech.koin.unit.domain.weather;

import static in.koreatech.koin.domain.weather.model.WeatherCondition.CLOUDY;
import static in.koreatech.koin.domain.weather.model.WeatherCondition.OVERCAST;
import static in.koreatech.koin.domain.weather.model.WeatherCondition.RAIN;
import static in.koreatech.koin.domain.weather.model.WeatherCondition.RAIN_AND_SNOW;
import static in.koreatech.koin.domain.weather.model.WeatherCondition.SHOWER;
import static in.koreatech.koin.domain.weather.model.WeatherCondition.SNOW;
import static in.koreatech.koin.domain.weather.model.WeatherCondition.SUNNY;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.weather.model.WeatherCondition;

class WeatherConditionTest {

    @Test
    void 강수형태가_없으면_하늘상태로_날씨를_정한다() {
        assertThat(WeatherCondition.from("1", "0")).isEqualTo(SUNNY);
        assertThat(WeatherCondition.from("3", "0")).isEqualTo(CLOUDY);
        assertThat(WeatherCondition.from("4", "0")).isEqualTo(OVERCAST);
    }

    @Test
    void 강수형태가_있으면_하늘상태보다_강수형태를_우선한다() {
        assertThat(WeatherCondition.from("1", "1")).isEqualTo(RAIN);
        assertThat(WeatherCondition.from("1", "2")).isEqualTo(RAIN_AND_SNOW);
        assertThat(WeatherCondition.from("1", "3")).isEqualTo(SNOW);
        assertThat(WeatherCondition.from("1", "4")).isEqualTo(SHOWER);
    }
}
