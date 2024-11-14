package in.koreatech.koin.domain.bus.dto.city;

import java.util.Collections;
import java.util.List;

public record CityBusRouteApiResponse(
    InnerResponse response
) {

    public List<CityBusRoute> extractBusRouteInfo() {
        if (!response().header().resultCode().equals("00")
            || response().body().totalCount() == 0) {
            return Collections.emptyList();
        }
        return response().body().items().item();
    }

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
        List<CityBusRoute> item
    ) {
    }
}
