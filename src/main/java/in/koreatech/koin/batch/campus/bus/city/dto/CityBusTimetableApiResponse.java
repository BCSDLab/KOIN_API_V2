package in.koreatech.koin.batch.campus.bus.city.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CityBusTimetableApiResponse(
    List<InnerRoute> resultList
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InnerRoute(
        @JsonProperty("ROUTE_ID")
        Long routeId
    ) {

    }
}
