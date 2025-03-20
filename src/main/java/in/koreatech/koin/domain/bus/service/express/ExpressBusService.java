package in.koreatech.koin.domain.bus.service.express;

import static in.koreatech.koin.domain.bus.enums.BusStation.KOREATECH;
import static in.koreatech.koin.domain.bus.enums.BusStation.TERMINAL;
import static in.koreatech.koin.domain.bus.enums.BusType.EXPRESS;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.service.express.dto.ExpressBusRemainTime;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import in.koreatech.koin.domain.bus.service.model.BusTimetable;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCache;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusRoute;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusTimetable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressBusService {

    private final ExpressBusCacheRepository expressBusCacheRepository;

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
}
