package in.koreatech.koin.unit.admin.bus;

import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest;
import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest.InnerNodeInfoRequest;
import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest.InnerRouteInfoRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;

class AdminShuttleBusUpdateRequestTest {

    private static final List<String> WEEKDAYS = List.of("MON", "TUE", "WED", "THU", "FRI");

    private InnerAdminShuttleBusUpdateRequest createRequest(List<String> runningDays) {
        return new InnerAdminShuttleBusUpdateRequest(
            "천안・아산",
            "순환",
            "천안 셔틀",
            null,
            List.of(new InnerNodeInfoRequest("한기대", null)),
            List.of(new InnerRouteInfoRequest("1회", "(천안역→본교)", runningDays, List.of("08:00")))
        );
    }

    @Test
    @DisplayName("신규 시간표 생성 시 요청의 운행 요일이 엔티티에 반영된다")
    void mapRunningDaysToEntity() {
        List<RouteInfo> routeInfos = createRequest(WEEKDAYS).toRouteInfoEntity();

        assertThat(routeInfos).hasSize(1);
        assertThat(routeInfos.get(0).getRunningDays()).isEqualTo(WEEKDAYS);
        assertThat(routeInfos.get(0).getArrivalTime()).containsExactly("08:00");
    }

    @Test
    @DisplayName("운행 요일을 전달하지 않으면 엔티티의 운행 요일이 비어있다")
    void mapNullRunningDaysToEntity() {
        List<RouteInfo> routeInfos = createRequest(null).toRouteInfoEntity();

        assertThat(routeInfos.get(0).getRunningDays()).isNull();
    }
}
