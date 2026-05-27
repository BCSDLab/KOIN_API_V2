package in.koreatech.koin.domain.weather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.domain.weather.dto.WeatherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Weather: 날씨", description = "병천 날씨 정보를 조회한다")
public interface WeatherApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples =
            @ExampleObject(
                value = """
                    {
                      "temperature": 21,
                      "weather": "맑음"
                    }
                    """
            ))),
            @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "병천 날씨 조회")
    @GetMapping("/weather")
    ResponseEntity<WeatherResponse> getWeather();
}
