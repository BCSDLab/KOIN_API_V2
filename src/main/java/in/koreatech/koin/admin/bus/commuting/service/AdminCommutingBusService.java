package in.koreatech.koin.admin.bus.commuting.service;

import static in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest.InnerAdminCommutingBusUpdateRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest;
import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.commuting.repository.AdminCommutingBusRepository;
import in.koreatech.koin.admin.bus.shuttle.enums.RunningDays;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCommutingBusService {

    private final AdminCommutingBusRepository adminCommutingBusRepository;

    @Transactional
    public void updateCommutingBusTimetable(
        SemesterType semesterType,
        AdminCommutingBusUpdateRequest request
    ) {
        Map<TimetableKey, ShuttleBusRoute> preparedTimetables = new LinkedHashMap<>();
        for (InnerAdminCommutingBusUpdateRequest commutingBusUpdateRequest : request.commutingBusTimetables()) {
            ShuttleBusRegion region = ShuttleBusRegion.convertFrom(commutingBusUpdateRequest.region());
            ShuttleRouteType routeType = ShuttleRouteType.convertFrom(commutingBusUpdateRequest.routeType());
            routeType.validateCommuting();

            // 등하교 버스는 validateCommuting() 으로 주중 노선만 허용되므로 운행 요일이 주중으로 결정된다.
            List<String> runningDays = RunningDays.WEEKDAYS.getDays();
            TimetableKey key = new TimetableKey(region, routeType,
                commutingBusUpdateRequest.routeName(), commutingBusUpdateRequest.subName());

            Optional<ShuttleBusRoute> shuttleBusRoute = preparedTimetables.containsKey(key)
                ? Optional.of(preparedTimetables.get(key))
                : adminCommutingBusRepository.findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                    semesterType.getDescription(),
                    region,
                    routeType,
                    commutingBusUpdateRequest.routeName(),
                    commutingBusUpdateRequest.subName()
                );

            ShuttleBusRoute route;
            if (shuttleBusRoute.isPresent()) {
                route = shuttleBusRoute.get();
                route.updateCommutingBusRoute(
                    commutingBusUpdateRequest.toNodeInfoEntity(),
                    commutingBusUpdateRequest.toRouteInfoEntity(runningDays)
                );
            } else {
                route = ShuttleBusRoute.builder()
                    .semesterType(semesterType.getDescription())
                    .region(region)
                    .routeType(routeType)
                    .routeName(commutingBusUpdateRequest.routeName())
                    .subName(commutingBusUpdateRequest.subName())
                    .nodeInfo(commutingBusUpdateRequest.toNodeInfoEntity())
                    .routeInfo(commutingBusUpdateRequest.toRouteInfoEntity(runningDays))
                    .build();
            }
            preparedTimetables.put(key, route);
        }

        // 동일 노선 요청을 누적하고 전체 입력 검증이 성공한 경우에만 저장한다.
        preparedTimetables.values().forEach(adminCommutingBusRepository::save);
    }

    private record TimetableKey(ShuttleBusRegion region, ShuttleRouteType routeType, String routeName, String subName) {
    }
}
