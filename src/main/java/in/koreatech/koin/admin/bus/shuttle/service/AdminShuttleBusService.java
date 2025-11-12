package in.koreatech.koin.admin.bus.shuttle.service;

import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest;
import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable;
import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable.NodeInfo;
import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimeTable.RouteInfo;
import in.koreatech.koin.admin.bus.shuttle.repository.AdminShuttleBusTimeTableRepository;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminShuttleBusService {

    private final AdminShuttleBusTimeTableRepository adminShuttleBusTimeTableRepository;

    @Transactional
    public void updateShuttleBusTimeTable(AdminShuttleBusUpdateRequest request, SemesterType semesterType) {
        for (InnerAdminShuttleBusUpdateRequest shuttleBusUpdateRequest : request.shuttleBusTimeTables()) {
            String region = ShuttleBusRegion.convertFrom(shuttleBusUpdateRequest.region()).name();
            String routeType = ShuttleRouteType.convertFrom(shuttleBusUpdateRequest.routeType()).name();
            String routeName = shuttleBusUpdateRequest.routeName();
            String subName = shuttleBusUpdateRequest.subName();

            Optional<ShuttleBusTimeTable> optionalTimeTable =
                adminShuttleBusTimeTableRepository.findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                    semesterType.getDescription(), region, routeType, routeName, subName
                );

            ShuttleBusTimeTable timeTable = optionalTimeTable
                .map(existing -> {
                    existing.updateNodeInfo(
                        shuttleBusUpdateRequest.nodeInfo().stream()
                            .map(node -> NodeInfo.of(node.name(), node.detail()))
                            .toList()
                    );
                    existing.updateRouteInfo(
                        shuttleBusUpdateRequest.routeInfo().stream()
                            .map(route -> RouteInfo.of(
                                    route.name(), route.detail(), route.arrivalTime()
                                )
                            ).toList()
                    );
                    return existing;
                })
                .orElseGet(
                    () -> ShuttleBusTimeTable.fromRequest(shuttleBusUpdateRequest, semesterType.getDescription())
                );

            adminShuttleBusTimeTableRepository.save(timeTable);
        }
    }
}
