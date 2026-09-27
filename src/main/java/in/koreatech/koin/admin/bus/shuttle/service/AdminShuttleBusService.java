package in.koreatech.koin.admin.bus.shuttle.service;

import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.REQUIRED_SHUTTLE_RUNNING_DAYS;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest;
import in.koreatech.koin.admin.bus.shuttle.enums.UpdateMode;
import in.koreatech.koin.admin.bus.shuttle.repository.AdminShuttleBusTimetableRepository;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.NodeInfo;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminShuttleBusService {

    private static final Set<String> VALID_RUNNING_DAYS = Set.of(
        "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
    );

    private final AdminShuttleBusTimetableRepository adminShuttleBusTimetableRepository;

    @Transactional
    public void updateShuttleBusTimetable(AdminShuttleBusUpdateRequest request, SemesterType semesterType) {
        updateShuttleBusTimetable(request, semesterType, UpdateMode.PARTIAL);
    }

    @Transactional
    public void updateShuttleBusTimetable(
        AdminShuttleBusUpdateRequest request,
        SemesterType semesterType,
        UpdateMode updateMode
    ) {
        validateRequestBatch(request);
        UpdateMode mode = updateMode == null ? UpdateMode.PARTIAL : updateMode;
        Map<TimetableKey, ShuttleBusRoute> preparedTimetables = new LinkedHashMap<>();
        Set<TimetableKey> replacementKeys = new HashSet<>();

        for (InnerAdminShuttleBusUpdateRequest timetableRequest : request.shuttleBusTimetables()) {
            ShuttleBusRegion region = ShuttleBusRegion.convertFrom(timetableRequest.region());
            ShuttleRouteType routeType = ShuttleRouteType.convertFrom(timetableRequest.routeType());
            TimetableKey key = new TimetableKey(
                region,
                routeType,
                timetableRequest.routeName(),
                timetableRequest.subName()
            );
            List<NodeInfo> nodeInfos = timetableRequest.toNodeInfoEntity();
            List<RouteInfo> routeInfos = timetableRequest.toRouteInfoEntity();

            if (mode == UpdateMode.REPLACE) {
                if (!replacementKeys.add(key)) {
                    throw invalidRequest("전체 교체 요청에는 같은 노선 키를 여러 번 포함할 수 없습니다.");
                }
                validateRouteRequest(nodeInfos, routeInfos, true);
                Optional<ShuttleBusRoute> existing = findExistingTimetable(
                    preparedTimetables, key, semesterType
                );
                ShuttleBusRoute prepared = existing
                    .map(ShuttleBusRoute::copy)
                    .orElseGet(() -> createTimetable(
                        semesterType, region, routeType, timetableRequest, nodeInfos, routeInfos
                    ));
                prepared.replaceRoute(nodeInfos, routeInfos);
                preparedTimetables.put(key, prepared);
                continue;
            }

            boolean alreadyPrepared = preparedTimetables.containsKey(key);
            Optional<ShuttleBusRoute> existing = findExistingTimetable(preparedTimetables, key, semesterType);
            if (existing.isPresent()) {
                validateRouteRequest(nodeInfos, routeInfos, false);
                ShuttleBusRoute prepared = alreadyPrepared
                    ? existing.get()
                    : existing.get().copy();
                prepared.updateCommutingBusRoute(nodeInfos, routeInfos);
                preparedTimetables.put(key, prepared);
                continue;
            }

            validateRouteRequest(nodeInfos, routeInfos, true);
            ShuttleBusRoute prepared = createTimetable(
                semesterType, region, routeType, timetableRequest, nodeInfos, routeInfos
            );
            preparedTimetables.put(key, prepared);
        }

        preparedTimetables.values().forEach(adminShuttleBusTimetableRepository::save);
    }

    private Optional<ShuttleBusRoute> findExistingTimetable(
        Map<TimetableKey, ShuttleBusRoute> preparedTimetables,
        TimetableKey key,
        SemesterType semesterType
    ) {
        if (preparedTimetables.containsKey(key)) {
            return Optional.of(preparedTimetables.get(key));
        }
        return adminShuttleBusTimetableRepository
            .findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                semesterType.getDescription(),
                key.region().name(),
                key.routeType().name(),
                key.routeName(),
                key.subName()
            );
    }

    private ShuttleBusRoute createTimetable(
        SemesterType semesterType,
        ShuttleBusRegion region,
        ShuttleRouteType routeType,
        InnerAdminShuttleBusUpdateRequest request,
        List<NodeInfo> nodeInfos,
        List<RouteInfo> routeInfos
    ) {
        return ShuttleBusRoute.builder()
            .semesterType(semesterType.getDescription())
            .region(region)
            .routeType(routeType)
            .routeName(request.routeName())
            .subName(request.subName())
            .nodeInfo(nodeInfos)
            .routeInfo(routeInfos)
            .build();
    }

    private void validateRequestBatch(AdminShuttleBusUpdateRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.shuttleBusTimetables())) {
            throw invalidRequest("버스 시간표 정보 목록은 비어 있을 수 없습니다.");
        }
        for (InnerAdminShuttleBusUpdateRequest timetableRequest : request.shuttleBusTimetables()) {
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
                    throw CustomException.of(REQUIRED_SHUTTLE_RUNNING_DAYS);
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
