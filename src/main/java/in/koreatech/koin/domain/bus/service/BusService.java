package in.koreatech.koin.domain.bus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.model.BusCourse;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusStation;
import in.koreatech.koin.domain.bus.model.BusType;
import in.koreatech.koin.domain.bus.model.Route;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    /**
     * 궁금한 점
     * - 천안역-> 한기대의 경우: 천안 셔틀 하교(시간표: 한기대->터미널->천안역->...->한기대)에서 천안역->한기대 부분도 포함해야 하나? 시간표명은 하교인데 등교를 위해 사용할 수 있는가??
     * -> O
     * - 테스트를 위해 이번 API 요청 파라미터에 현재 시각을 기입할 수 있도록 하자는 한수형의 의견이 있었는데, 지금 바로 도입해야 할지(하위호환성)
     * -> X. 테스트 코드는 LocalDateTime을 mocking한다.
     */

    public BusRemainTimeResponse getBusRemainTime(String busTypeStr, String departStr, String arrivalStr) {
        BusStation departStation = BusStation.from(departStr);
        BusStation arrivalStation = BusStation.from(arrivalStr);
        BusType busType = BusType.from(busTypeStr);
        List<BusRemainTime> remainTimes = busRepository.getByBusType(busType)
            .stream()
            .map(BusCourse::getRoutes)
            .flatMap(routes ->
                routes.stream()
                    .filter(Route::isRunning)
                    .map(route -> route.getArrivalInfos().get(0))
                    .map(Route.ArrivalNode::getArrivalTime)
                    .map(BusRemainTime::from)
                    .filter(BusRemainTime::isBefore)
            )
            .distinct()
            .sorted()
            .toList();

        return BusRemainTimeResponse.of(busType, remainTimes);
    }
}
