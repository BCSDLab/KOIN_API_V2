package in.koreatech.koin.domain.weather.dto;

import java.util.List;

public record WeatherApiResponse(
    InnerResponse response
) {

    public record InnerResponse(
        InnerHeader header,
        InnerBody body
    ) {

    }

    public record InnerHeader(
        String resultCode,
        String resultMsg
    ) {

    }

    public record InnerBody(
        String dataType,
        InnerItems items,
        Integer numOfRows,
        Integer pageNo,
        Integer totalCount
    ) {

    }

    public record InnerItems(
        List<WeatherForecastItem> item
    ) {

    }

    public record WeatherForecastItem(
        String baseDate,
        String baseTime,
        String category,
        String fcstDate,
        String fcstTime,
        String fcstValue,
        Integer nx,
        Integer ny
    ) {

    }
}
