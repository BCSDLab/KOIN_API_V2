package in.koreatech.koin.batch.campus.bus.city.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CityBusRouteApiResponse(
    List<CityBusRouteInfo> resultList
) {
}
