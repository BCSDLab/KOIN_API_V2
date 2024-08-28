package in.koreatech.koin.domain.bus.util.express;

import static in.koreatech.koin.domain.bus.model.enums.BusStation.KOREATECH;
import static in.koreatech.koin.domain.bus.model.enums.BusStation.TERMINAL;
import static in.koreatech.koin.domain.bus.model.enums.BusType.EXPRESS;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
import in.koreatech.koin.global.domain.callcontoller.CallController;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressBusService {

    private final List<ExpressBusClient> expressBusTypes;
    private final List<ExpressBusClient> apiCallListByRatio = new ArrayList<>();
    private final ExpressBusCacheRepository expressBusCacheRepository;
    private final CallController<ExpressBusClient> callController;

    public void storeRemainTimeByRatio() {
        ExpressBusClient selectedBus = callController.getInstanceByRatio(expressBusTypes, apiCallListByRatio);
        List<ExpressBusClient> fallBackableTypes = new ArrayList<>(expressBusTypes);
        while (true) {
            try {
                System.out.println("호출할 버스: " + selectedBus);
                selectedBus.storeRemainTime();
                System.out.println("성공!");
                break;
            } catch (CallNotPermittedException e) {
                System.out.println("다른 API를 호출합니다.(서킷브레이커)");
                selectedBus = callController.fallBack(true, selectedBus, fallBackableTypes);
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException("호출할 수 있는 버스 API가 없습니다.");
            } catch (Exception e) {
                System.out.println("다른 API를 호출합니다.(일반)");
                selectedBus = callController.fallBack(false, selectedBus, fallBackableTypes);
            }
        }
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
}
