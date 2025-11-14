package in.koreatech.koin.admin.bus.shuttle.service;

import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest;
import in.koreatech.koin.admin.bus.shuttle.repository.AdminShuttleBusTimetableRepository;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.NodeInfo;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;
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
                .orElseGet(
                    () -> ShuttleBusRoute.builder()
                        .semesterType(semesterType.getDescription())
                        .region(region)
                        .routeType(routeType)
                        .routeName(routeName)
                        .subName(subName)
                        .nodeInfo(newNodeInfos)
                        .routeInfo(newRouteInfos)
                        .build()
                );

            adminShuttleBusTimetableRepository.save(timetable);
        }
    }
}
