package in.koreatech.koin.domain.bus.batch.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.batch.client.CityBusClient;
import in.koreatech.koin.domain.bus.batch.client.CityBusRouteClient;
import in.koreatech.koin.domain.bus.batch.response.CityBusArrival;
import in.koreatech.koin.domain.bus.batch.response.CityBusRoute;
import in.koreatech.koin.domain.bus.batch.exception.BusOpenApiException;
import in.koreatech.koin.domain.bus.model.city.CityBusCache;
import in.koreatech.koin.domain.bus.model.city.CityBusCacheInfo;
import in.koreatech.koin.domain.bus.model.enums.BusStationNode;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.bus.repository.CityBusRouteCacheRepository;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CityBusCacheService {

    private final CityBusRouteCacheRepository cityBusRouteCacheRepository;
    private final CityBusCacheRepository cityBusCacheRepository;

    private final CityBusClient cityBusClient;
    private final CityBusRouteClient cityBusRouteClient;
    private final VersionRepository versionRepository;
    private final Clock clock;

    public void cacheRemainTime() {
        LocalDateTime updatedAt = LocalDateTime.now(clock);
        BusStationNode.getNodeIds().stream()
            .map(nodeId -> {
                try {
                    return cityBusClient.fetchBusArrivalList(nodeId);
                } catch (BusOpenApiException ignored) {
                    return Collections.<CityBusArrival>emptyList();
                }
            })
            .filter(arrivalInfos -> !arrivalInfos.isEmpty())
            .map(arrivalInfos -> CityBusCache.of(
                arrivalInfos.get(0).nodeid(),
                toCityBusCacheInfo(arrivalInfos, updatedAt)
            ))
            .forEach(cityBusCacheRepository::save);
        versionRepository.getByType(VersionType.CITY).update(clock);
    }

    private List<CityBusCacheInfo> toCityBusCacheInfo(List<CityBusArrival> arrivalInfos, LocalDateTime updatedAt) {
        return arrivalInfos.stream()
            .map(busArrivalInfo -> busArrivalInfo.toCityBusCacheInfo(updatedAt))
            .toList();
    }

    public void cacheCityBusRoute() {
        BusStationNode.getNodeIds().forEach((nodeId) -> {
            try {
                Set<CityBusRoute> routes = Set.copyOf(cityBusRouteClient.fetchBusRouteList(nodeId));
                cityBusRouteCacheRepository.save(CityBusRoute.toCityBusRouteCache(nodeId, routes));
            } catch (BusOpenApiException ignored) {
            }
        });
    }
}
