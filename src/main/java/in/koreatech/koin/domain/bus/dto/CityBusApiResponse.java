package in.koreatech.koin.domain.bus.dto;

import java.util.List;
import java.util.Optional;

import in.koreatech.koin.domain.bus.model.city.CityBusArrival;
import lombok.Builder;

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
        Integer totalCount
    ) {

    }

    public record InnerItems(
        List<CityBusArrival> item
    ) {

    }
}
