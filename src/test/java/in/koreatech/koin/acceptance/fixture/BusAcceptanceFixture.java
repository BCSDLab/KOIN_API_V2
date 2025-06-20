package in.koreatech.koin.acceptance.fixture;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.city.model.CityBusTimetable;
import in.koreatech.koin.domain.bus.service.city.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.service.shuttle.ShuttleBusRepository;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class BusAcceptanceFixture {

    @Autowired
    private final ShuttleBusRepository shuttleBusRepository;

    @Autowired
    private final CityBusTimetableRepository cityBusTimetableRepository;

    public BusAcceptanceFixture(
        ShuttleBusRepository shuttleBusRepository,
        CityBusTimetableRepository cityBusTimetableRepository
    ) {
        this.shuttleBusRepository = shuttleBusRepository;
        this.cityBusTimetableRepository = cityBusTimetableRepository;
    }

    public void 버스_시간표_등록() {
        shuttleBusRepository.save(
            ShuttleBusRoute.builder()
                .id("1")
                .routeName("터미널/천안역")
                .routeType(ShuttleRouteType.SHUTTLE)
                .region(ShuttleBusRegion.CHEONAN_ASAN)
                .semesterType("정규학기")
                .subName(null)
                .nodeInfo(List.of(
                        ShuttleBusRoute.NodeInfo.builder()
                            .name("한기대")
                            .build(),
                        ShuttleBusRoute.NodeInfo.builder()
                            .name("신계초,운전리,연춘리")
                            .build(),
                        ShuttleBusRoute.NodeInfo.builder()
                            .name("천안역(학화호두과자)")
                            .build(),
                        ShuttleBusRoute.NodeInfo.builder()
                            .name("터미널(신세계 앞 횡단보도)")
                            .build()))
                .routeInfo(List.of(
                        ShuttleBusRoute.RouteInfo.builder()
                            .name("주중")
                            .runningDays(List.of("MON", "TUE", "WED", "THU", "FRI"))
                            .arrivalTime(List.of("18:10", "정차", "18:50", "18:55"))
                            .build()))
                .build()
        );
    }

    public void 시내버스_시간표_등록() {
        cityBusTimetableRepository.save(
            CityBusTimetable.builder()
                .updatedAt(LocalDateTime.of(2024, 7, 19, 19, 0))
                .busInfo(
                    CityBusTimetable.BusInfo.builder()
                        .number(400L)
                        .depart("병천3리")
                        .arrival("종합터미널")
                        .build()
                )
                .busTimetables(
                    List.of(
                        CityBusTimetable.BusTimetable.builder()
                            .dayOfWeek("평일")
                            .departInfo(List.of("06:00", "07:00")).build(),
                        CityBusTimetable.BusTimetable.builder()
                            .dayOfWeek("주말")
                            .departInfo(List.of("08:00", "09:00")).build()
                    )
                )
                .build()
        );
    }
}
