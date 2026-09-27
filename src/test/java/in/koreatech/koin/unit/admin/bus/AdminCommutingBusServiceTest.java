package in.koreatech.koin.unit.admin.bus;

import static in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest.InnerAdminCommutingBusUpdateRequest;
import static in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest.InnerAdminCommutingBusUpdateRequest.InnerNodeInfo;
import static in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest.InnerAdminCommutingBusUpdateRequest.InnerRouteInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.admin.bus.commuting.dto.AdminCommutingBusUpdateRequest;
import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.commuting.repository.AdminCommutingBusRepository;
import in.koreatech.koin.admin.bus.commuting.service.AdminCommutingBusService;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.NodeInfo;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;

@ExtendWith(MockitoExtension.class)
class AdminCommutingBusServiceTest {

    @InjectMocks
    private AdminCommutingBusService adminCommutingBusService;

    @Mock
    private AdminCommutingBusRepository adminCommutingBusRepository;

    private static final List<String> WEEKDAYS = List.of("MON", "TUE", "WED", "THU", "FRI");

    private AdminCommutingBusUpdateRequest createRequest() {
        return new AdminCommutingBusUpdateRequest(List.of(
            new InnerAdminCommutingBusUpdateRequest(
                "천안・아산",
                "주중",
                "천안 등하교",
                null,
                List.of(new InnerNodeInfo("한기대", null)),
                List.of(new InnerRouteInfo("등교", null, List.of("08:00")))
            )
        ));
    }

    private void givenExistingTimetable(Optional<ShuttleBusRoute> result) {
        when(adminCommutingBusRepository
            .findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                anyString(), any(), any(), anyString(), any()
            )
        ).thenReturn(result);
    }

    private ShuttleBusRoute captureSavedRoute() {
        ArgumentCaptor<ShuttleBusRoute> captor = ArgumentCaptor.forClass(ShuttleBusRoute.class);
        verify(adminCommutingBusRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("신규 등하교 시간표 생성 시 운행 요일이 주중으로 저장된다")
    void saveWeekdaysOnCreate() {
        givenExistingTimetable(Optional.empty());

        adminCommutingBusService.updateCommutingBusTimetable(SemesterType.REGULAR, createRequest());

        assertThat(captureSavedRoute().getRouteInfo().get(0).getRunningDays()).isEqualTo(WEEKDAYS);
    }

    @Test
    @DisplayName("운행 요일이 비어있던 기존 등하교 시간표는 갱신해도 비어 있는 상태를 유지한다")
    void preservesMissingDaysOnUpdate() {
        ShuttleBusRoute existing = ShuttleBusRoute.builder()
            .routeName("천안 등하교")
            .nodeInfo(List.of(NodeInfo.builder().name("한기대").build()))
            .routeInfo(List.of(
                RouteInfo.builder()
                    .name("등교")
                    .arrivalTime(List.of("07:00"))
                    .build()
            ))
            .build();
        givenExistingTimetable(Optional.of(existing));

        adminCommutingBusService.updateCommutingBusTimetable(SemesterType.REGULAR, createRequest());

        RouteInfo saved = captureSavedRoute().getRouteInfo().get(0);
        assertThat(saved.getRunningDays()).isNull();
        assertThat(saved.getArrivalTime()).containsExactly("08:00");
    }

    @Test
    @DisplayName("통학 시간표에 명시한 운행 요일은 노선 형태와 무관하게 저장된다")
    void savesExplicitRunningDays() {
        givenExistingTimetable(Optional.empty());
        InnerAdminCommutingBusUpdateRequest item = new InnerAdminCommutingBusUpdateRequest(
            "천안・아산",
            "주중",
            "천안 등하교",
            null,
            List.of(new InnerNodeInfo("한기대", null)),
            List.of(new InnerRouteInfo("등교", null, List.of("SAT"), List.of("08:00")))
        );

        adminCommutingBusService.updateCommutingBusTimetable(
            SemesterType.REGULAR,
            new AdminCommutingBusUpdateRequest(List.of(item))
        );

        assertThat(captureSavedRoute().getRouteInfo().get(0).getRunningDays())
            .containsExactly("SAT");
    }

}
