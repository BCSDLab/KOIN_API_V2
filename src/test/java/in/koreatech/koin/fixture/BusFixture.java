package in.koreatech.koin.fixture;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.model.mongo.Route;
import in.koreatech.koin.domain.bus.repository.BusRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class BusFixture {

    @Autowired
    private final BusRepository busRepository;

    public BusFixture(BusRepository busRepository) {
        this.busRepository = busRepository;
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

    public BusFixtureBuilder builder() {
        return new BusFixtureBuilder();
    }

    public final class BusFixtureBuilder {

    }
}
