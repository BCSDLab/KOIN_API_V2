package in.koreatech.koin.admin.bus;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.REQUIRED_SHUTTLE_RUNNING_DAYS;

import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.NodeInfo;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;
import in.koreatech.koin.global.exception.CustomException;

public final class TimetableValidator {

    private static final Set<String> VALID_RUNNING_DAYS = Set.of(
        "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
    );

    private TimetableValidator() {
    }

    public static void validateTimetable(
        String region, String routeType, String routeName, List<?> nodes, List<?> routes
    ) {
        if (!StringUtils.hasText(region) || !StringUtils.hasText(routeType) || !StringUtils.hasText(routeName)
            || CollectionUtils.isEmpty(nodes) || CollectionUtils.isEmpty(routes)) {
            throw CustomException.of(INVALID_REQUEST_BODY, "노선과 정류장, 회차 정보는 필수입니다.");
        }
        if (nodes.stream().anyMatch(node -> node == null)) {
            throw CustomException.of(INVALID_REQUEST_BODY, "정류장 정보는 비어 있을 수 없습니다.");
        }
        if (routes.stream().anyMatch(route -> route == null)) {
            throw CustomException.of(INVALID_REQUEST_BODY, "회차 정보는 비어 있을 수 없습니다.");
        }
    }

    public static void validateRoutes(List<NodeInfo> nodes, List<RouteInfo> routes, boolean requireRunningDays) {
        ShuttleBusRoute.validateRouteShape(nodes, routes);
        for (RouteInfo route : routes) {
            List<String> days = route.getRunningDays();
            if (CollectionUtils.isEmpty(days)) {
                if (requireRunningDays) {
                    throw CustomException.of(REQUIRED_SHUTTLE_RUNNING_DAYS);
                }
            } else if (days.stream().anyMatch(day -> day == null || !VALID_RUNNING_DAYS.contains(day))) {
                throw CustomException.of(INVALID_REQUEST_BODY, "운행 요일 코드가 올바르지 않습니다.");
            }
        }
    }
}
