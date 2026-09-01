package in.koreatech.koin.admin.bus.shuttle.service;

import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest;
import static in.koreatech.koin.global.code.ApiResponseCode.REQUIRED_SHUTTLE_RUNNING_DAYS;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest;
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
        for (InnerAdminShuttleBusUpdateRequest shuttleBusUpdateRequest : request.shuttleBusTimetables()) {
            ShuttleBusRegion region = ShuttleBusRegion.convertFrom(shuttleBusUpdateRequest.region());
            ShuttleRouteType routeType = ShuttleRouteType.convertFrom(shuttleBusUpdateRequest.routeType());
            String routeName = shuttleBusUpdateRequest.routeName();
            String subName = shuttleBusUpdateRequest.subName();

            Optional<ShuttleBusRoute> optionalTimetable =
                adminShuttleBusTimetableRepository.findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                    semesterType.getDescription(), region.name(), routeType.name(), routeName, subName
                );

            List<NodeInfo> newNodeInfos = shuttleBusUpdateRequest.toNodeInfoEntity();
            List<RouteInfo> newRouteInfos = shuttleBusUpdateRequest.toRouteInfoEntity();

            ShuttleBusRoute timetable = optionalTimetable
                .map(existing -> {
                    existing.updateCommutingBusRoute(newNodeInfos, newRouteInfos);
                    return existing;
                })
                .orElseGet(() -> {
                    validateRunningDaysPresent(newRouteInfos);

                    return ShuttleBusRoute.builder()
                        .semesterType(semesterType.getDescription())
                        .region(region)
                        .routeType(routeType)
                        .routeName(routeName)
                        .subName(subName)
                        .nodeInfo(newNodeInfos)
                        .routeInfo(newRouteInfos)
                        .build();
                });

            adminShuttleBusTimetableRepository.save(timetable);
        }
    }

    /**
     * 운행 요일이 없는 회차는 요일 필터에 걸리지 않아 교통편 조회에서 누락된다.
     * 기존 시간표는 저장된 운행 요일을 유지할 수 있지만, 신규 시간표는 대체할 값이 없으므로 생성 시점에 검증한다.
     */
    private void validateRunningDaysPresent(List<RouteInfo> routeInfos) {
        boolean hasEmptyRunningDays = routeInfos.stream()
            .anyMatch(routeInfo -> CollectionUtils.isEmpty(routeInfo.getRunningDays()));

        if (hasEmptyRunningDays) {
            throw CustomException.of(REQUIRED_SHUTTLE_RUNNING_DAYS);
        }
    }
}
