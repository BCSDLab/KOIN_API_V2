package in.koreatech.koin.domain.bus.util;

import static in.koreatech.koin.domain.bus.model.enums.BusStation.KOREATECH;
import static in.koreatech.koin.domain.bus.model.enums.BusStation.TERMINAL;
import static in.koreatech.koin.domain.bus.model.enums.BusType.EXPRESS;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.bus.dto.ExpressBusRemainTime;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusTimetable;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRoute;
import in.koreatech.koin.domain.bus.model.express.ExpressBusTimetable;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.repository.VersionRepository;

@Transactional(readOnly = true)
public abstract class ExpressBusClient<T, U> {

    protected final VersionRepository versionRepository;
    protected final ExpressBusCacheRepository expressBusCacheRepository;
    protected final RestTemplate restTemplate;
    protected final Clock clock;

    public ExpressBusClient(
        VersionRepository versionRepository,
        RestTemplate restTemplate,
        Clock clock,
        ExpressBusCacheRepository expressBusCacheRepository
    ) {
        this.versionRepository = versionRepository;
        this.restTemplate = restTemplate;
        this.clock = clock;
        this.expressBusCacheRepository = expressBusCacheRepository;
    }

    public SingleBusTimeResponse searchBusTime(
        String busType,
        BusStation depart, BusStation arrival,
        LocalDateTime targetTime
    ) {
        List<ExpressBusRemainTime> remainTimes = getBusRemainTime(depart, arrival);
        if (remainTimes.isEmpty()) {
            return null;
        }

        LocalTime arrivalTime = remainTimes.stream()
            .filter(expressBusRemainTime -> targetTime.toLocalTime().isBefore(expressBusRemainTime.getBusArrivalTime()))
            .min(Comparator.naturalOrder())
            .map(BusRemainTime::getBusArrivalTime)
            .orElse(null);

        return new SingleBusTimeResponse(busType, arrivalTime);
    }

    public List<ExpressBusRemainTime> getBusRemainTime(BusStation depart, BusStation arrival) {
        String busCacheId = ExpressBusCache.generateId(
            new ExpressBusRoute(depart.getName(), arrival.getName()));
        return getStoredRemainTime(busCacheId);
    }

    private List<ExpressBusRemainTime> getStoredRemainTime(String busCacheId) {
        Optional<ExpressBusCache> expressBusCache = expressBusCacheRepository.findById(busCacheId);
        if (expressBusCache.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExpressBusCacheInfo> busArrivals = expressBusCache.get().getBusInfos();
        return getExpressBusRemainTime(busArrivals);
    }

    private List<ExpressBusRemainTime> getExpressBusRemainTime(
        List<ExpressBusCacheInfo> busArrivals
    ) {
        return busArrivals.stream()
            .map(it -> new ExpressBusRemainTime(it.depart(), EXPRESS.getName()))
            .toList();
    }

    public List<? extends BusTimetable> getExpressBusTimetable(String direction) {
        BusStation depart = null;
        BusStation arrival = null;

        if ("from".equals(direction)) {
            depart = KOREATECH;
            arrival = TERMINAL;
        }
        if ("to".equals(direction)) {
            depart = TERMINAL;
            arrival = KOREATECH;
        }

        if (depart == null || arrival == null) {
            throw new UnsupportedOperationException();
        }

        String busCacheId = ExpressBusCache.generateId(
            new ExpressBusRoute(depart.getName(), arrival.getName()));

        ExpressBusCache expressBusCache = expressBusCacheRepository.getById(busCacheId);
        if (Objects.isNull(expressBusCache)) {
            return Collections.emptyList();
        }
        List<ExpressBusCacheInfo> busArrivals = expressBusCache.getBusInfos();

        return busArrivals
            .stream()
            .map(ExpressBusTimetable::from)
            .toList();
    }

    public abstract void storeRemainTimeByOpenApi();

    protected abstract T getOpenApiResponse(BusStation depart, BusStation arrival);

    protected abstract U getBusApiURL(BusStation depart, BusStation arrival);

    protected abstract List<?> extractBusArrivalInfo(T ExpressBusResponse);
}
