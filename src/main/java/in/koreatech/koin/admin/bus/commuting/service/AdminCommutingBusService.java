package in.koreatech.koin.admin.bus.commuting.service;

import static in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest.InnerAdminCommutingBusUpdateRequest;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest;
import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.commuting.repository.AdminCommutingBusRepository;
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
        for (InnerAdminCommutingBusUpdateRequest commutingBusUpdateRequest : request.commutingBusTimetables()) {
            ShuttleBusRegion region = ShuttleBusRegion.convertFrom(commutingBusUpdateRequest.region());
            ShuttleRouteType routeType = ShuttleRouteType.convertFrom(commutingBusUpdateRequest.routeType());
            routeType.validateCommuting();

            Optional<ShuttleBusRoute> shuttleBusRoute = adminCommutingBusRepository
                .findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                    semesterType.getDescription(),
                    region,
                    routeType,
                    commutingBusUpdateRequest.routeName(),
                    commutingBusUpdateRequest.subName()
                );

            if (shuttleBusRoute.isPresent()) {
                ShuttleBusRoute route = shuttleBusRoute.get();
                route.updateCommutingBusRoute(
                    commutingBusUpdateRequest.toNodeInfoEntity(),
                    commutingBusUpdateRequest.toRouteInfoEntity()
                );
                adminCommutingBusRepository.save(route);
            } else {
                adminCommutingBusRepository.save(ShuttleBusRoute.builder()
                    .semesterType(semesterType.getDescription())
                    .region(region)
                    .routeType(routeType)
                    .routeName(commutingBusUpdateRequest.routeName())
                    .subName(commutingBusUpdateRequest.subName())
                    .nodeInfo(commutingBusUpdateRequest.toNodeInfoEntity())
                    .routeInfo(commutingBusUpdateRequest.toRouteInfoEntity())
                    .build());
            }
        }
    }
}
