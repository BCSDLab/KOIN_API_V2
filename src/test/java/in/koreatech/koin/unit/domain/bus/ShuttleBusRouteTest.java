package in.koreatech.koin.unit.domain.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.NodeInfo;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

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

    @Test
    @DisplayName("동명 회차 입력 개수가 다르면 기존 노선과 회차를 변경하지 않는다")
    void rejectsAmbiguousDuplicateRoundCountWithoutPartialMutation() {
        ShuttleBusRoute route = ShuttleBusRoute.builder()
            .nodeInfo(List.of(createNodeInfo("기존 정류장")))
            .routeInfo(List.of(
                RouteInfo.builder()
                    .name("토요일 오후")
                    .runningDays(SATURDAY)
                    .arrivalTime(List.of("14:25"))
                    .build(),
                RouteInfo.builder()
                    .name("토요일 오후")
                    .runningDays(SATURDAY)
                    .arrivalTime(List.of("18:30"))
                    .build()
            ))
            .build();

        assertThatThrownBy(() -> route.updateCommutingBusRoute(
            List.of(createNodeInfo("새 정류장")),
            List.of(RouteInfo.builder()
                .name("토요일 오후")
                .runningDays(SATURDAY)
                .arrivalTime(List.of("19:00"))
                .build())
        ))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ApiResponseCode.INVALID_REQUEST_BODY);

        assertThat(route.getNodeInfo()).extracting(NodeInfo::getName)
            .containsExactly("기존 정류장");
        assertThat(route.getRouteInfo().get(0).getArrivalTime()).containsExactly("14:25");
        assertThat(route.getRouteInfo().get(1).getArrivalTime()).containsExactly("18:30");
    }

    @Test
    @DisplayName("동명 회차도 각 요청의 운행 요일 누락과 명시 갱신을 개별 적용한다")
    void preservesAndUpdatesRunningDaysPerDuplicateRound() {
        ShuttleBusRoute route = ShuttleBusRoute.builder()
            .routeInfo(List.of(
                RouteInfo.builder()
                    .name("일요일 오후")
                    .runningDays(SATURDAY)
                    .arrivalTime(List.of("15:30"))
                    .build(),
                RouteInfo.builder()
                    .name("일요일 오후")
                    .runningDays(SATURDAY)
                    .arrivalTime(List.of("17:25"))
                    .build()
            ))
            .build();

        route.updateCommutingBusRoute(
            List.of(),
            List.of(
                RouteInfo.builder()
                    .name("일요일 오후")
                    .runningDays(null)
                    .arrivalTime(List.of("16:00"))
                    .build(),
                RouteInfo.builder()
                    .name("일요일 오후")
                    .runningDays(List.of("SUN"))
                    .arrivalTime(List.of("17:30"))
                    .build()
            )
        );

        assertThat(route.getRouteInfo().get(0).getRunningDays()).containsExactly("SAT");
        assertThat(route.getRouteInfo().get(0).getArrivalTime()).containsExactly("16:00");
        assertThat(route.getRouteInfo().get(1).getRunningDays()).containsExactly("SUN");
        assertThat(route.getRouteInfo().get(1).getArrivalTime()).containsExactly("17:30");
    }
}
