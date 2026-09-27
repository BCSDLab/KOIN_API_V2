package in.koreatech.koin.admin.bus.commuting.service;

import static in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest.InnerAdminCommutingBusUpdateRequest;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import in.koreatech.koin.admin.bus.TimetableValidator;
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
            ShuttleBusRoute prepared = preparedTimetables.get(key);
            if (prepared == null) {
                prepared = adminCommutingBusRepository.findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                    semesterType.getDescription(), region, routeType, key.routeName(), key.subName())
                    .map(ShuttleBusRoute::copy).orElse(null);
            }
            List<RouteInfo> routeInfos = timetableRequest.toRouteInfoEntity(prepared == null ? WEEKDAYS : null);
            TimetableValidator.validateRoutes(nodeInfos, routeInfos, false);
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
            } else {
                prepared.updateCommutingBusRoute(nodeInfos, routeInfos);
            }
            preparedTimetables.put(key, prepared);
        }

        preparedTimetables.values().forEach(adminCommutingBusRepository::save);
    }

    private void validateRequestBatch(AdminCommutingBusUpdateRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.commutingBusTimetables())) {
            throw invalidRequest("버스 노선 정보 목록은 비어 있을 수 없습니다.");
        }
        for (InnerAdminCommutingBusUpdateRequest timetableRequest : request.commutingBusTimetables()) {
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
