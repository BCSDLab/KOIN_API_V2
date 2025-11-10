package in.koreatech.koin.admin.bus.commuting.service;

import static in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest.InnerAdminCommutingBusUpdateRequest;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_SHUTTLE_ROUTE_TYPE;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest;
import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.commuting.repository.AdminCommutingBusRepository;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCommutingBusQueryService {

    private final AdminCommutingBusRepository adminCommutingBusRepository;

    @Transactional
    public void updateCommutingBusTimetable(
        SemesterType semesterType,
        AdminCommutingBusUpdateRequest request
    ) {
        for (InnerAdminCommutingBusUpdateRequest commutingBusUpdateRequest : request.commutingBusTimetables()) {
            ShuttleBusRegion region = ShuttleBusRegion.convertFrom(commutingBusUpdateRequest.region());
            ShuttleRouteType routeType = ShuttleRouteType.convertFrom(commutingBusUpdateRequest.routeType());
            if (routeType.isNotCommuting()) {
                throw CustomException.of(INVALID_SHUTTLE_ROUTE_TYPE, "shuttleRouteType: " + routeType.name());
            }

            Optional<ShuttleBusRoute> shuttleBusRote = adminCommutingBusRepository
                .findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                    semesterType.getDescription(),
                    region,
                    routeType,
                    commutingBusUpdateRequest.routeName(),
                    commutingBusUpdateRequest.subName()
                );

            if (shuttleBusRote.isPresent()) {
                ShuttleBusRoute route = shuttleBusRote.get();
                ShuttleBusRoute updatedRoute = ShuttleBusRoute.builder()
                    .id(route.getId())
                    .semesterType(route.getSemesterType())
                    .region(route.getRegion())
                    .routeType(route.getRouteType())
                    .routeName(route.getRouteName())
                    .subName(route.getSubName())
                    .nodeInfo(commutingBusUpdateRequest.toEntityNodeInfo())
                    .routeInfo(commutingBusUpdateRequest.toEntityRouteInfo())
                    .build();
                adminCommutingBusRepository.save(updatedRoute);
            } else {
                adminCommutingBusRepository.save(ShuttleBusRoute.builder()
                    .semesterType(semesterType.getDescription())
                    .region(region)
                    .routeType(routeType)
                    .routeName(commutingBusUpdateRequest.routeName())
                    .subName(commutingBusUpdateRequest.subName())
                    .nodeInfo(commutingBusUpdateRequest.toEntityNodeInfo())
                    .routeInfo(commutingBusUpdateRequest.toEntityRouteInfo())
                    .build());
            }
        }
    }
}
