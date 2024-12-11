package in.koreatech.koin.domain.bus.service;

import static in.koreatech.koin.domain.bus.dto.ShuttleBusTimetableResponse.NodeInfoResponse;
import static in.koreatech.koin.domain.bus.dto.ShuttleBusTimetableResponse.RouteInfoResponse;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.bus.dto.ShuttleBusRoutesResponse;
import in.koreatech.koin.domain.bus.dto.ShuttleBusRoutesResponse.RouteName;
import in.koreatech.koin.domain.bus.dto.ShuttleBusRoutesResponse.RouteRegion;
import in.koreatech.koin.domain.bus.dto.ShuttleBusRoutesResponse.RouteSemester;
import in.koreatech.koin.domain.bus.dto.ShuttleBusTimetableResponse;
import in.koreatech.koin.domain.bus.model.mongo.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.repository.ShuttleBusRepository;
import in.koreatech.koin.domain.version.dto.VersionMessageResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShuttleBusService {

    private final ShuttleBusRepository shuttleBusRepository;
    private final VersionService versionService;

    public ShuttleBusRoutesResponse getShuttleBusRoutes() {
        VersionMessageResponse version = versionService.getVersionWithMessage("shuttle_bus_timetable");
        List<ShuttleBusRoute> shuttleBusRoutes = shuttleBusRepository.findBySemesterType(version.title());
        return toRoutesResponse(shuttleBusRoutes, version);
    }

    private ShuttleBusRoutesResponse toRoutesResponse(List<ShuttleBusRoute> shuttleBusRoutes,
        VersionMessageResponse versionMessageResponse) {
        List<RouteRegion> categories = mapCategories(shuttleBusRoutes);
        RouteSemester routeSemester = new RouteSemester(versionMessageResponse.title(),
            versionMessageResponse.content());
        return new ShuttleBusRoutesResponse(categories, routeSemester);
    }

    private List<RouteRegion> mapCategories(List<ShuttleBusRoute> shuttleBusRoutes) {
        return shuttleBusRoutes.stream()
            .collect(Collectors.groupingBy(ShuttleBusRoute::getRegion))
            .entrySet().stream()
            .map(entry -> new RouteRegion(entry.getKey(), mapRouteNames(entry.getValue())))
            .sorted()
            .toList();
    }

    private List<RouteName> mapRouteNames(List<ShuttleBusRoute> routes) {
        return routes.stream()
            .map(route -> new RouteName(route.getId(), route.getRouteType(), route.getRouteName(), route.getSubName()))
            .sorted()
            .toList();
    }

    public ShuttleBusTimetableResponse getShuttleBusTimetable(String id) {
        ShuttleBusRoute shuttleBusRoute = shuttleBusRepository.getById(id);
        return toTimetableResponse(shuttleBusRoute);
    }

    private ShuttleBusTimetableResponse toTimetableResponse(ShuttleBusRoute shuttleBusRoute) {
        List<NodeInfoResponse> nodeInfoResponses = shuttleBusRoute.getNodeInfo().stream()
            .map(node -> new NodeInfoResponse(node.getName(), node.getDetail()))
            .toList();
        List<RouteInfoResponse> routeInfoResponses = shuttleBusRoute.getRouteInfo().stream()
            .map(route -> new RouteInfoResponse(route.getName(), route.getArrivalTime()))
            .toList();
        return new ShuttleBusTimetableResponse(
            shuttleBusRoute.getId(),
            shuttleBusRoute.getRegion(),
            shuttleBusRoute.getRouteType(),
            shuttleBusRoute.getRouteName(),
            shuttleBusRoute.getSubName(),
            nodeInfoResponses,
            routeInfoResponses
        );
    }
}
