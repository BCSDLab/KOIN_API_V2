package in.koreatech.koin.batch.campus.bus.city.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CityBusRouteInfo(

    @JsonProperty("ROUTE_ID")
    Long routeId,

    @JsonProperty("ROUTE_NAME")
    String routeName,

    @JsonProperty("ROUTE_DIRECTION")
    String routeDirection,

    @JsonProperty("RELAY_AREACODE")
    String relayAreaCode,

    @JsonProperty("ROUTE_EXPLAIN")
    String routeExplain,

    @JsonProperty("ST_STOP_NAME")
    String stName,

    @JsonProperty("ED_STOP_NAME")
    String edName
) {
}
