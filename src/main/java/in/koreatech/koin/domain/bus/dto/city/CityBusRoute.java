package in.koreatech.koin.domain.bus.dto.city;

import static in.koreatech.koin.domain.bus.model.city.CityBusRouteCache.CACHE_EXPIRE_MINUTE;

import java.util.Set;
import java.util.stream.Collectors;

import in.koreatech.koin.domain.bus.model.city.CityBusRouteCache;
import lombok.Builder;

@Builder
public record CityBusRoute(
    String endnodenm, // 종점, 병천3리
    String routeid, // 노선 ID, CAB285000142
    Long routeno, // 노선 번호, 400
    String routetp, // 노선 유형, 일반버스
    String startnodenm // 기점, 종합터미널
) {

    public static CityBusRouteCache toCityBusRouteCache(String nodeId, Set<CityBusRoute> busRoutes) {
        return CityBusRouteCache.builder()
            .id(nodeId)
            .busNumbers(busRoutes.stream()
                .map(CityBusRoute::routeno)
                .collect(Collectors.toSet())
            )
            .expiration(CACHE_EXPIRE_MINUTE)
            .build();
    }
}
