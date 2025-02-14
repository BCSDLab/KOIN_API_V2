package in.koreatech.koin.fixture;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.service.shuttle.model.BusCourse;
import in.koreatech.koin.domain.bus.service.city.model.CityBusTimetable;
import in.koreatech.koin.domain.bus.service.shuttle.model.Route;
import in.koreatech.koin.domain.bus.service.shuttle.BusRepository;
import in.koreatech.koin.domain.bus.service.city.repository.CityBusTimetableRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class BusFixture {

    @Autowired
    private final BusRepository busRepository;

    @Autowired
    private final CityBusTimetableRepository cityBusTimetableRepository;

    public BusFixture(BusRepository busRepository, CityBusTimetableRepository cityBusTimetableRepository) {
        this.busRepository = busRepository;
        this.cityBusTimetableRepository = cityBusTimetableRepository;
    }

    public void 버스_시간표_등록() {
        busRepository.save(
            BusCourse.builder()
                .busType("shuttle")
                .region("천안")
                .direction("from")
                .routes(
                    List.of(
                        Route.builder()
                            .routeName("주중")
                            .runningDays(List.of("MON", "TUE", "WED", "THU", "FRI"))
                            .arrivalInfos(
                                List.of(
                                    Route.ArrivalNode.builder()
                                        .nodeName("한기대")
                                        .arrivalTime("18:10")
                                        .build(),
                                    Route.ArrivalNode.builder()
                                        .nodeName("신계초,운전리,연춘리")
                                        .arrivalTime("정차")
                                        .build(),
                                    Route.ArrivalNode.builder()
                                        .nodeName("천안역(학화호두과자)")
                                        .arrivalTime("18:50")
                                        .build(),
                                    Route.ArrivalNode.builder()
                                        .nodeName("터미널(신세계 앞 횡단보도)")
                                        .arrivalTime("18:55")
                                        .build()
                                )
                            )
                            .build()
                    )
                )
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
