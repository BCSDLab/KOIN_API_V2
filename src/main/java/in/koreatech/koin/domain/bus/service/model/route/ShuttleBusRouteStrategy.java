package in.koreatech.koin.domain.bus.service.model.route;

import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.enums.BusRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.ShuttleBusRepository;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusSimpleRoute;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShuttleBusRouteStrategy implements BusRouteStrategy {

    private final ShuttleBusRepository shuttleBusRepository;
    private final VersionRepository versionRepository;

    /**
     * 출발/도착 정류장이 여러 번 나타나는 경우를 고려한 노선 필터링 및 출발 시간 변환
     */
    @Override
    public List<ScheduleInfo> findSchedule(BusRouteCommand command) {
        // 운영 학기 정보 가져오기
        Version version = versionRepository.getByTypeAndIsPrevious(VersionType.SHUTTLE, false);
        String semesterType = version.getTitle();

        // 운영 학기에 맞는 셔틀버스 데이터 가져오기
        List<ShuttleBusSimpleRoute> routes = shuttleBusRepository.findBySemesterType(
            semesterType,
            convertDateToDayOfWeek(command)
        );

        // 출발/도착 정류장을 기준으로 ScheduleInfo 생성
        return routes.stream()
            .flatMap(route -> mapToScheduleInfo(route, command.depart().getQueryName(),
                command.arrive().getQueryName()).stream())
            .collect(Collectors.toList());
    }

    private String convertDateToDayOfWeek(BusRouteCommand command) {
        return command.date().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();
    }

    private List<ScheduleInfo> mapToScheduleInfo(ShuttleBusSimpleRoute route, String depart, String arrive) {
        List<String> nodes = route.getNodeName();
        List<String> arrivalTimes = route.getArrivalTime();

        // 출발 정류장과 도착 정류장에 해당하는 모든 시간 찾기
        List<LocalTime> departTimes = findTimesForNode(nodes, arrivalTimes, depart);
        List<LocalTime> arriveTimes = findTimesForNode(nodes, arrivalTimes, arrive);

        // 유효한 노선인지 확인하고 ScheduleInfo 생성
        return departTimes.stream()
            .flatMap(departTime -> arriveTimes.stream()
                .filter(departTime::isBefore) // 출발 시간 < 도착 시간일 경우 필터링
                .map(validDepartTime -> createScheduleInfo(route, validDepartTime)) // ScheduleInfo 생성
            )
            .toList();
    }

    private ScheduleInfo createScheduleInfo(ShuttleBusSimpleRoute route, LocalTime departTime) {
        return new ScheduleInfo(
            "shuttle",
            route.getRouteName(),
            departTime
        );
    }

    /**
     * 특정 정류장에 해당하는 모든 시간을 반환
     * 도착 정류장, 출발 정류장에 정차하는 시간은 각각 반환
     */
    private List<LocalTime> findTimesForNode(List<String> nodes, List<String> arrivalTimes, String targetNode) {
        List<LocalTime> times = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).contains(targetNode) && isValidTime(arrivalTimes.get(i))) {
                times.add(LocalTime.parse(arrivalTimes.get(i)));
            }
        }
        return times;
    }

    /**
     * 시간 값이 유효한지 확인
     */
    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time); // 시간 변환 시도
            return true;
        } catch (Exception e) {
            return false; // 유효하지 않은 시간은 제외
        }
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.SHUTTLE || type == BusRouteType.ALL;
    }
}
