package in.koreatech.koin.acceptance;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.bus.model.BusCourse;
import in.koreatech.koin.domain.bus.model.BusDirection;
import in.koreatech.koin.domain.bus.model.BusInfoCache;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusStation;
import in.koreatech.koin.domain.bus.model.BusType;
import in.koreatech.koin.domain.bus.model.CityBusArrivalInfo;
import in.koreatech.koin.domain.bus.model.CityBusCache;
import in.koreatech.koin.domain.bus.model.Route;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class BusApiTest extends AcceptanceTest {

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private CityBusCacheRepository cityBusCacheRepository;

    @Test
    @DisplayName("다음 셔틀버스까지 남은 시간을 조회한다.")
    void getNextShuttleBusRemainTime() {
        final String arrivalTime = "18:10";

        when(clock.instant()).thenReturn(
            ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z")).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        BusType busType = BusType.from("shuttle");
        BusStation depart = BusStation.from("koreatech");
        BusStation arrival = BusStation.from("terminal");

        BusCourse busCourse = BusCourse.builder()
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
                                    .arrivalTime(arrivalTime)
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
            .build();
        busRepository.save(busCourse);
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("bus_type", busType.name().toLowerCase())
            .param("depart", depart.name())
            .param("arrival", arrival.name())
            .get("/bus")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("bus_type"))
                    .isEqualTo(busType.name().toLowerCase());
                softly.assertThat((Long)response.body().jsonPath().get("now_bus.bus_number")).isNull();
                softly.assertThat(response.body().jsonPath().getLong("now_bus.remain_time")).isEqualTo(
                    BusRemainTime.from(arrivalTime).getRemainSeconds(clock));
                softly.assertThat((Long)response.body().jsonPath().get("next_bus.bus_number")).isNull();
                softly.assertThat((Long)response.body().jsonPath().get("next_bus.remain_time")).isNull();
            }
        );
    }

    @Test
    @DisplayName("다음 시내버스까지 남은 시간을 조회한다.")
    void getNextCityBusRemainTime() {
        final ZonedDateTime now = ZonedDateTime.now();
        final long remainTime = 600L;
        final long busNumber = 400;

        when(clock.instant()).thenReturn(now.toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        BusType busType = BusType.from("city");
        BusStation depart = BusStation.from("koreatech");
        BusStation arrival = BusStation.from("terminal");
        BusDirection direction = BusStation.getDirection(depart, arrival);

        Version version = versionRepository.save(
            Version.builder()
                .version("20240_1711255839")
                .type("city_bus_timetable")
                .build()
        );

        cityBusCacheRepository.save(
            CityBusCache.create(
                depart.getNodeId(direction),
                List.of(BusInfoCache.from(
                    CityBusArrivalInfo.builder()
                        .routeno(busNumber)
                        .arrtime(remainTime)
                        .build(),
                    version.getUpdatedAt())
                )
            )
        );

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("bus_type", busType.name().toLowerCase())
            .param("depart", depart.name())
            .param("arrival", arrival.name())
            .get("/bus")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("bus_type"))
                    .isEqualTo(busType.name().toLowerCase());
                softly.assertThat((Long)response.body().jsonPath().getLong("now_bus.bus_number")).isEqualTo(busNumber);
                softly.assertThat(response.body().jsonPath().getLong("now_bus.remain_time")).isEqualTo(
                    BusRemainTime.from(remainTime, clock).getRemainSeconds(clock));
                softly.assertThat((Long)response.body().jsonPath().get("next_bus.bus_number")).isNull();
                softly.assertThat((Long)response.body().jsonPath().get("next_bus.remain_time")).isNull();
            }
        );
    }
}
