package in.koreatech.koin.domain.weather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import in.koreatech.koin.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {

    private final WeatherService weatherService;

    @GetMapping("/weather")
    public ResponseEntity<WeatherResponse> getWeather() {
        WeatherResponse response = weatherService.getWeather();
        return ResponseEntity.ok(response);
    }
}
