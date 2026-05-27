package in.koreatech.koin.domain.weather.scheduler;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherService weatherService;

    @EventListener(ApplicationReadyEvent.class)
    public void refreshWeatherOnStartup() {
        refreshWeather();
    }

    @Scheduled(cron = "0 0 * * * *")
    public void refreshWeather() {
        try {
            weatherService.refreshWeather();
        } catch (Exception e) {
            log.warn("날씨 스케줄링 과정에서 오류가 발생했습니다.", e);
        }
    }
}
