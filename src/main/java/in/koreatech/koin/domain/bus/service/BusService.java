package in.koreatech.koin.domain.bus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.model.BusCourse;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusStation;
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
     * - now_bus와 next_bus 응답은 오늘 남은 버스가 없으면 null이 응답되는 것인지?(클라 출력: 운행 정보 없음)    (BusRemainTime 주석처리된 부분)
     * - 응답 객체를 보면 시내버스만 버스 번호를 함께 반환하는데, 그럼 Response DTO를 두 개 둬야 하는가? 그럼 controller 메서드에서는 반환하는 객체 타입을 명시할 수 없는데 괜찮은가?
     * - 테스트를 위해 이번 API 요청 파라미터에 현재 시각을 기입할 수 있도록 하자는 한수형의 의견이 있었는데, 지금 바로 도입해야 할지(하위호환성)
     */

    public BusRemainTimeResponse getBusRemainTime(String busType, String depart, String arrival) {
        List<BusCourse> all = busRepository.findAll();
        String direction = BusStation.getDirection(depart, arrival);
        List<BusCourse> foundBusCourses = busRepository.getByBusTypeAndDirection(busType, direction);

        List<BusRemainTime> remainTimes = foundBusCourses.stream()
            .map(BusCourse::getRoutes)
            .flatMap(routes ->
                routes.stream()
                    .filter(Route::isRunning)
                    .map(route -> route.getArrivalInfos().get(0))
                    .map(Route.ArrivalNode::getArrivalTime)
                    .map(BusRemainTime::from)
                    .filter(BusRemainTime::isBefore)
            )
            .sorted()
            .toList();

        return BusRemainTimeResponse.of(busType, remainTimes);
    }
}
