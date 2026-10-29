package in.koreatech.koin.admin.bus.shuttle.service;

import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import in.koreatech.koin.admin.bus.TimetableValidator;
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
        boolean replace = updateMode == UpdateMode.REPLACE;
        Map<TimetableKey, ShuttleBusRoute> preparedTimetables = new LinkedHashMap<>();

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

            if (replace && preparedTimetables.containsKey(key)) {
                throw invalidRequest("전체 교체 요청에는 같은 노선 키를 여러 번 포함할 수 없습니다.");
            }
            ShuttleBusRoute prepared = preparedTimetables.get(key);
            if (prepared == null) {
                prepared = adminShuttleBusTimetableRepository
                    .findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                        semesterType.getDescription(), region.name(), routeType.name(),
                        key.routeName(), key.subName())
                    .map(ShuttleBusRoute::copy).orElse(null);
            }
            TimetableValidator.validateRoutes(nodeInfos, routeInfos, replace || prepared == null);
            if (prepared == null) {
                prepared = ShuttleBusRoute.builder()
                    .semesterType(semesterType.getDescription())
                    .region(region)
                    .routeType(routeType)
                    .routeName(key.routeName())
                    .subName(key.subName())
                    .nodeInfo(nodeInfos)
                    .routeInfo(routeInfos)
                    .build();
            } else if (!replace) {
                prepared.updateCommutingBusRoute(nodeInfos, routeInfos);
            }
            if (replace) {
                prepared.replaceRoute(nodeInfos, routeInfos);
            }
            preparedTimetables.put(key, prepared);
        }

        preparedTimetables.values().forEach(adminShuttleBusTimetableRepository::save);
    }

    private void validateRequestBatch(AdminShuttleBusUpdateRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.shuttleBusTimetables())) {
            throw invalidRequest("버스 시간표 정보 목록은 비어 있을 수 없습니다.");
        }
        for (InnerAdminShuttleBusUpdateRequest timetableRequest : request.shuttleBusTimetables()) {
            if (timetableRequest == null) {
                throw invalidRequest("노선과 정류장, 회차 정보는 필수입니다.");
            }
            TimetableValidator.validateTimetable(
                timetableRequest.region(), timetableRequest.routeType(), timetableRequest.routeName(),
                timetableRequest.nodeInfo(), timetableRequest.routeInfo());
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
