package in.koreatech.koin.admin.bus.commuting.service;

import static in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest.InnerAdminCommutingBusUpdateRequest;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest;
import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.commuting.repository.AdminCommutingBusRepository;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.NodeInfo;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCommutingBusService {

    private static final List<String> WEEKDAYS = List.of("MON", "TUE", "WED", "THU", "FRI");
    private static final Set<String> VALID_RUNNING_DAYS = Set.of(
        "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
    );

    private final AdminCommutingBusRepository adminCommutingBusRepository;

    @Transactional
    public void updateCommutingBusTimetable(
        SemesterType semesterType,
        AdminCommutingBusUpdateRequest request
    ) {
        validateRequestBatch(request);
        Map<TimetableKey, ShuttleBusRoute> preparedTimetables = new LinkedHashMap<>();

        for (InnerAdminCommutingBusUpdateRequest timetableRequest : request.commutingBusTimetables()) {
            ShuttleBusRegion region = ShuttleBusRegion.convertFrom(timetableRequest.region());
            ShuttleRouteType routeType = ShuttleRouteType.convertFrom(timetableRequest.routeType());
            routeType.validateCommuting();
            TimetableKey key = new TimetableKey(
                region,
                routeType,
                timetableRequest.routeName(),
                timetableRequest.subName()
            );
            List<NodeInfo> nodeInfos = timetableRequest.toNodeInfoEntity();
            List<RouteInfo> requestedRouteInfos = timetableRequest.toRouteInfoEntity();
            Optional<ShuttleBusRoute> existing = findExistingTimetable(
                preparedTimetables, key, semesterType
            );

            if (existing.isPresent()) {
                validateRouteRequest(nodeInfos, requestedRouteInfos, false);
                ShuttleBusRoute prepared = preparedTimetables.containsKey(key)
                    ? existing.get()
                    : existing.get().copy();
                prepared.updateCommutingBusRoute(nodeInfos, requestedRouteInfos);
                preparedTimetables.put(key, prepared);
                continue;
            }

            List<RouteInfo> routeInfos = timetableRequest.toRouteInfoEntity(WEEKDAYS);
            validateRouteRequest(nodeInfos, routeInfos, true);
            ShuttleBusRoute prepared = ShuttleBusRoute.builder()
                .semesterType(semesterType.getDescription())
                .region(region)
                .routeType(routeType)
                .routeName(timetableRequest.routeName())
                .subName(timetableRequest.subName())
                .nodeInfo(nodeInfos)
                .routeInfo(routeInfos)
                .build();
            preparedTimetables.put(key, prepared);
        }

        preparedTimetables.values().forEach(adminCommutingBusRepository::save);
    }

    private Optional<ShuttleBusRoute> findExistingTimetable(
        Map<TimetableKey, ShuttleBusRoute> preparedTimetables,
        TimetableKey key,
        SemesterType semesterType
    ) {
        if (preparedTimetables.containsKey(key)) {
            return Optional.of(preparedTimetables.get(key));
        }
        return adminCommutingBusRepository.findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
            semesterType.getDescription(),
            key.region(),
            key.routeType(),
            key.routeName(),
            key.subName()
        );
    }

    private void validateRequestBatch(AdminCommutingBusUpdateRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.commutingBusTimetables())) {
            throw invalidRequest("버스 노선 정보 목록은 비어 있을 수 없습니다.");
        }
        for (InnerAdminCommutingBusUpdateRequest timetableRequest : request.commutingBusTimetables()) {
            if (timetableRequest == null
                || !StringUtils.hasText(timetableRequest.region())
                || !StringUtils.hasText(timetableRequest.routeType())
                || !StringUtils.hasText(timetableRequest.routeName())
                || CollectionUtils.isEmpty(timetableRequest.nodeInfo())
                || CollectionUtils.isEmpty(timetableRequest.routeInfo())) {
                throw invalidRequest("노선과 정류장, 회차 정보는 필수입니다.");
            }
            if (timetableRequest.nodeInfo().stream().anyMatch(node -> node == null)) {
                throw invalidRequest("정류장 정보는 비어 있을 수 없습니다.");
            }
            if (timetableRequest.routeInfo().stream().anyMatch(route -> route == null)) {
                throw invalidRequest("회차 정보는 비어 있을 수 없습니다.");
            }
        }
    }

    private void validateRouteRequest(
        List<NodeInfo> nodeInfos,
        List<RouteInfo> routeInfos,
        boolean requireRunningDays
    ) {
        ShuttleBusRoute.validateRouteShape(nodeInfos, routeInfos);
        for (RouteInfo routeInfo : routeInfos) {
            List<String> runningDays = routeInfo.getRunningDays();
            if (CollectionUtils.isEmpty(runningDays)) {
                if (requireRunningDays) {
                    throw invalidRequest("신규 통학 시간표에는 운행 요일이 필요합니다.");
                }
                continue;
            }
            if (runningDays.stream().anyMatch(day -> day == null || !VALID_RUNNING_DAYS.contains(day))) {
                throw invalidRequest("운행 요일 코드가 올바르지 않습니다.");
            }
        }
    }

    private static CustomException invalidRequest(String detail) {
        return CustomException.of(INVALID_REQUEST_BODY, detail);
    }

    private record TimetableKey(
        ShuttleBusRegion region,
        ShuttleRouteType routeType,
        String routeName,
        String subName
    ) {
    }
}
