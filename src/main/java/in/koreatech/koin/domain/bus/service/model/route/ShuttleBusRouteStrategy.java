package in.koreatech.koin.domain.bus.service.model.route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.enums.BusRouteType;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.ShuttleBusRepository;
import in.koreatech.koin.domain.bus.service.shuttle.internal.ShuttleRoutePathSelector;
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
        if (command.depart() == command.arrive()) {
            return List.of();
        }

        // 운영 학기 정보 가져오기
        Version version = versionRepository.getByTypeAndIsPrevious(VersionType.SHUTTLE, false);
        String semesterType = version.getTitle();

        // 운영 학기에 맞는 셔틀버스 데이터 가져오기
        List<ShuttleBusSimpleRoute> routes = shuttleBusRepository.findBySemesterType(
            semesterType,
            convertDateToDayOfWeek(command.date())
        );

        // 출발/도착 정류장을 기준으로 ScheduleInfo 생성
        return routes.stream()
            .filter(route -> ShuttleBusRegion.CHEONAN_ASAN.equals(route.getRegion()))
            .filter(route -> route.isArrayLengthsMatch())
            .flatMap(route -> mapToScheduleInfo(route, command).stream())
            .toList();
    }

    /**
     * 날짜를 요일 이니셜(ex: SAT)로 변환하는 함수
     */
    private String convertDateToDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();
    }

    private List<ScheduleInfo> mapToScheduleInfo(ShuttleBusSimpleRoute route, BusRouteCommand command) {
        if ("미운행".equals(route.getRouteName()) || "미운행".equals(route.getRouteInfo())) {
            return List.of();
        }
        return ShuttleRoutePathSelector.select(
                route.getNodeName(),
                route.getArrivalTime(),
                route.getRouteType(),
                route.getRouteType() == ShuttleRouteType.WEEKDAYS
                    ? route.getRouteInfo() : route.getRouteDetail(),
                command.depart(),
                command.arrive())
            .stream()
            .map(occurrence -> createScheduleInfo(route, occurrence.departureTime()))
            .distinct() // 중복제거 (record 타입: 필드명이 모두 같으면 중복)
            .toList();
    }

    private ScheduleInfo createScheduleInfo(ShuttleBusSimpleRoute route, LocalTime departureTime) {
        return new ScheduleInfo(
            "shuttle",
            route.getRouteName(),
            departureTime
        );
    }

    @Override
    public boolean support(BusRouteType type) {
        return type == BusRouteType.SHUTTLE || type == BusRouteType.ALL;
    }
}
