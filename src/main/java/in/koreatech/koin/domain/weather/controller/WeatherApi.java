package in.koreatech.koin.domain.weather.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.EXTERNAL_API_ERROR;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Weather: 날씨", description = "병천 날씨 정보를 조회한다")
public interface WeatherApi {

    @ApiResponseCodes({
        OK,
        EXTERNAL_API_ERROR
    })
    @Operation(
        summary = "병천 날씨 조회",
        description = """
            반환되는 날씨 종류는 총 7개입니다.

            - 1: 맑음
            - 2: 구름많음
            - 3: 흐림
            - 4: 비
            - 5: 비/눈
            - 6: 눈
            - 7: 소나기
            """
    )
    @GetMapping("/weather")
    ResponseEntity<WeatherResponse> getWeather();
}
