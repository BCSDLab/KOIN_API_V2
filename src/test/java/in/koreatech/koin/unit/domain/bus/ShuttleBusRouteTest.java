package in.koreatech.koin.unit.domain.bus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.NodeInfo;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;

class ShuttleBusRouteTest {

    private static final List<String> WEEKDAYS = List.of("MON", "TUE", "WED", "THU", "FRI");
    private static final List<String> SATURDAY = List.of("SAT");

    private ShuttleBusRoute createRoute(List<String> runningDays) {
        return ShuttleBusRoute.builder()
            .routeName("천안 셔틀")
            .nodeInfo(List.of(createNodeInfo("한기대")))
            .routeInfo(List.of(
                RouteInfo.builder()
                    .name("1회")
                    .detail("(천안역→본교)")
                    .runningDays(runningDays)
                    .arrivalTime(List.of("08:00", "08:30"))
                    .build()
            ))
            .build();
    }

    private NodeInfo createNodeInfo(String name) {
        return NodeInfo.builder()
            .name(name)
            .build();
    }

    private List<RouteInfo> createUpdatedRouteInfos(List<String> runningDays) {
        return List.of(
            RouteInfo.builder()
                .name("1회")
                .detail("(천안역→본교)")
                .runningDays(runningDays)
                .arrivalTime(List.of("09:00", "09:30"))
                .build()
        );
    }

    @Test
    @DisplayName("운행 요일을 전달하지 않으면 기존 운행 요일이 유지된다")
    void keepRunningDaysWhenNotGiven() {
        ShuttleBusRoute route = createRoute(WEEKDAYS);

        route.updateCommutingBusRoute(
            List.of(createNodeInfo("한기대")),
            createUpdatedRouteInfos(null)
        );

        RouteInfo updated = route.getRouteInfo().get(0);
        assertThat(updated.getRunningDays()).isEqualTo(WEEKDAYS);
        assertThat(updated.getArrivalTime()).containsExactly("09:00", "09:30");
    }

    @Test
    @DisplayName("운행 요일이 빈 배열이면 기존 운행 요일이 유지된다")
    void keepRunningDaysWhenEmpty() {
        ShuttleBusRoute route = createRoute(WEEKDAYS);

        route.updateCommutingBusRoute(
            List.of(createNodeInfo("한기대")),
            createUpdatedRouteInfos(List.of())
        );

        assertThat(route.getRouteInfo().get(0).getRunningDays()).isEqualTo(WEEKDAYS);
    }

    @Test
    @DisplayName("운행 요일을 전달하면 기존 운행 요일이 갱신된다")
    void updateRunningDays() {
        ShuttleBusRoute route = createRoute(WEEKDAYS);

        route.updateCommutingBusRoute(
            List.of(createNodeInfo("한기대")),
            createUpdatedRouteInfos(SATURDAY)
        );

        assertThat(route.getRouteInfo().get(0).getRunningDays()).isEqualTo(SATURDAY);
    }
}
