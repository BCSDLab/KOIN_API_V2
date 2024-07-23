package in.koreatech.koin.domain.bus.dto;

import java.util.List;

import in.koreatech.koin.domain.bus.model.city.CityBusArrival;

public record CityBusApiResponse(
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
        InnerItems items,
        Integer numOfRows,
        Integer pageNo,
        Integer totalCount
    ) {

    }

    public record InnerItems(
        List<CityBusArrival> item
    ) {

    }
}
