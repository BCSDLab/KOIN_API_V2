package in.koreatech.koin.unit.admin.bus;

import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest;
import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest.InnerNodeInfoRequest;
import static in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest.InnerAdminShuttleBusUpdateRequest.InnerRouteInfoRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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

import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest;
import in.koreatech.koin.admin.bus.shuttle.repository.AdminShuttleBusTimetableRepository;
import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusService;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.NodeInfo;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute.RouteInfo;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class AdminShuttleBusServiceTest {

    @InjectMocks
    private AdminShuttleBusService adminShuttleBusService;

    @Mock
    private AdminShuttleBusTimetableRepository adminShuttleBusTimetableRepository;

    private static final List<String> WEEKDAYS = List.of("MON", "TUE", "WED", "THU", "FRI");

    private AdminShuttleBusUpdateRequest createRequest(List<String> runningDays) {
        return new AdminShuttleBusUpdateRequest(List.of(
            new InnerAdminShuttleBusUpdateRequest(
                "천안・아산",
                "순환",
                "천안 셔틀",
                null,
                List.of(new InnerNodeInfoRequest("한기대", null)),
                List.of(new InnerRouteInfoRequest("1회", "(천안역→본교)", runningDays, List.of("08:00")))
            )
        ));
    }

    private ShuttleBusRoute createExistingRoute() {
        return ShuttleBusRoute.builder()
            .routeName("천안 셔틀")
            .nodeInfo(List.of(NodeInfo.builder().name("한기대").build()))
            .routeInfo(List.of(
                RouteInfo.builder()
                    .name("1회")
                    .runningDays(WEEKDAYS)
                    .arrivalTime(List.of("07:00"))
                    .build()
            ))
            .build();
    }

    private void givenExistingTimetable(Optional<ShuttleBusRoute> result) {
        when(adminShuttleBusTimetableRepository
            .findBySemesterTypeAndRegionAndRouteTypeAndRouteNameAndSubName(
                anyString(), anyString(), anyString(), anyString(), any()
            )
        ).thenReturn(result);
    }

    @Test
    @DisplayName("신규 시간표 생성 시 운행 요일이 없으면 예외가 발생한다")
    void throwExceptionWhenRunningDaysMissingOnCreate() {
        givenExistingTimetable(Optional.empty());

        assertThatThrownBy(() ->
            adminShuttleBusService.updateShuttleBusTimetable(createRequest(null), SemesterType.REGULAR)
        )
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ApiResponseCode.REQUIRED_SHUTTLE_RUNNING_DAYS);

        verify(adminShuttleBusTimetableRepository, never()).save(any());
    }

    @Test
    @DisplayName("신규 시간표 생성 시 운행 요일이 빈 배열이면 예외가 발생한다")
    void throwExceptionWhenRunningDaysEmptyOnCreate() {
        givenExistingTimetable(Optional.empty());

        assertThatThrownBy(() ->
            adminShuttleBusService.updateShuttleBusTimetable(createRequest(List.of()), SemesterType.REGULAR)
        )
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ApiResponseCode.REQUIRED_SHUTTLE_RUNNING_DAYS);

        verify(adminShuttleBusTimetableRepository, never()).save(any());
    }

    @Test
    @DisplayName("신규 시간표 생성 시 운행 요일이 저장된다")
    void saveRunningDaysOnCreate() {
        givenExistingTimetable(Optional.empty());

        adminShuttleBusService.updateShuttleBusTimetable(createRequest(WEEKDAYS), SemesterType.REGULAR);

        ArgumentCaptor<ShuttleBusRoute> captor = ArgumentCaptor.forClass(ShuttleBusRoute.class);
        verify(adminShuttleBusTimetableRepository).save(captor.capture());
        assertThat(captor.getValue().getRouteInfo().get(0).getRunningDays()).isEqualTo(WEEKDAYS);
    }

    @Test
    @DisplayName("기존 시간표는 운행 요일을 전달하지 않아도 기존 값을 유지한 채 저장된다")
    void keepRunningDaysOnUpdate() {
        givenExistingTimetable(Optional.of(createExistingRoute()));

        adminShuttleBusService.updateShuttleBusTimetable(createRequest(null), SemesterType.REGULAR);

        ArgumentCaptor<ShuttleBusRoute> captor = ArgumentCaptor.forClass(ShuttleBusRoute.class);
        verify(adminShuttleBusTimetableRepository).save(captor.capture());

        RouteInfo saved = captor.getValue().getRouteInfo().get(0);
        assertThat(saved.getRunningDays()).isEqualTo(WEEKDAYS);
        assertThat(saved.getArrivalTime()).containsExactly("08:00");
    }
}
