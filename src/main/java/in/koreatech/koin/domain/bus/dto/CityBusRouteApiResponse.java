package in.koreatech.koin.domain.bus.dto;

import java.util.List;

import in.koreatech.koin.domain.bus.model.city.CityBusArrival;
import in.koreatech.koin.domain.bus.model.city.CityBusRoute;

public record CityBusRouteApiResponse(
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
        Integer totalCount
    ) {

    }

    public record InnerItems(
        List<CityBusRoute> item
    ) {

    }
}
