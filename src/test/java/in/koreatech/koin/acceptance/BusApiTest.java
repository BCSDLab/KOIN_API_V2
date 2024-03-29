package in.koreatech.koin.acceptance;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.CityBusArrival;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.model.mongo.Route;
import in.koreatech.koin.domain.bus.model.redis.BusCache;
import in.koreatech.koin.domain.bus.model.redis.CityBusCache;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.bus.util.CityBusOpenApiClient;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
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

    @SpyBean
    private CityBusOpenApiClient cityBusOpenApiClient;

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
            .when()
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
                softly.assertThat((Long)response.body().jsonPath().get("now_bus.bus_number")).isNull();
                softly.assertThat(response.body().jsonPath().getLong("now_bus.remain_time")).isEqualTo(
                    BusRemainTime.from(arrivalTime).getRemainSeconds(clock));
                softly.assertThat((Long)response.body().jsonPath().get("next_bus.bus_number")).isNull();
                softly.assertThat((Long)response.body().jsonPath().get("next_bus.remain_time")).isNull();
            }
        );
    }

    @Test
    @DisplayName("다음 시내버스까지 남은 시간을 조회한다. - Redis")
    void getNextCityBusRemainTimeRedis() {
        final long remainTime = 600L;
        final long busNumber = 400;

        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        when(clock.instant())
            .thenReturn(
                ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z"))
                    .toInstant()
            );

        BusType busType = BusType.from("city");
        BusStation depart = BusStation.from("terminal");
        BusStation arrival = BusStation.from("koreatech");
        BusDirection direction = BusStation.getDirection(depart, arrival);

        Version version = versionRepository.save(
            Version.builder()
                .version("20240_1711255839")
                .type("city_bus_timetable")
                .build()
        );

        when(clock.instant())
            .thenReturn(
                ZonedDateTime.parse("2024-02-21 18:00:30 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z"))
                    .toInstant()
            );

        cityBusCacheRepository.save(
            CityBusCache.create(
                depart.getNodeId(direction),
                List.of(BusCache.from(
                    CityBusArrival.builder()
                        .routeno(busNumber)
                        .arrtime(remainTime)
                        .build(),
                    version.getUpdatedAt())
                )
            )
        );

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
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
                softly.assertThat((Long)response.body().jsonPath().getLong("now_bus.remain_time"))
                    .isEqualTo(
                        BusRemainTime.from(remainTime, version.getUpdatedAt().toLocalTime()).getRemainSeconds(clock));
                softly.assertThat(response.body().jsonPath().getObject("next_bus.bus_number", Long.class)).isNull();
                softly.assertThat(response.body().jsonPath().getObject("next_bus.remain_time", Long.class)).isNull();
            }
        );
    }

    @Test
    @DisplayName("다음 시내버스까지 남은 시간을 조회한다. - OpenApi")
    void getNextCityBusRemainTimeOpenApi() {
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        when(clock.instant())
            .thenReturn(
                ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z"))
                    .toInstant()
            );

        BusType busType = BusType.from("city");
        BusStation depart = BusStation.from("terminal");
        BusStation arrival = BusStation.from("koreatech");
        BusDirection direction = BusStation.getDirection(depart, arrival);

        versionRepository.save(
            Version.builder()
                .version("20240_1711255839")
                .type("city_bus_timetable")
                .build()
        );

        when(clock.instant())
            .thenReturn(
                ZonedDateTime.parse("2024-02-21 21:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z"))
                    .toInstant()
            );

        String json = """
            {
              "response": {
                "header": {
                  "resultCode": "00",
                  "resultMsg": "NORMAL SERVICE."
                },
                "body": {
                  "items": {
                    "item": [
                      {
                        "arrprevstationcnt": 3,
                        "arrtime": 600,
                        "nodeid": "CAB285000686",
                        "nodenm": "종합터미널",
                        "routeid": "CAB285000003",
                        "routeno": 400,
                        "routetp": "일반버스",
                        "vehicletp": "저상버스"
                      },
                      {
                        "arrprevstationcnt": 10,
                        "arrtime": 800,
                        "nodeid": "CAB285000686",
                        "nodenm": "종합터미널",
                        "routeid": "CAB285000024",
                        "routeno": 405,
                        "routetp": "일반버스",
                        "vehicletp": "일반차량"
                      }
                    ]
                  },
                  "numOfRows": 30,
                  "pageNo": 1,
                  "totalCount": 2
                }
              }
            }
            """;

        String nodeId = depart.getNodeId(direction);
        when(cityBusOpenApiClient.getOpenApiResponse(nodeId)).thenReturn(json);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("bus_type", busType.name().toLowerCase())
            .param("depart", depart.name())
            .param("arrival", arrival.name())
            .get("/bus")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Version version = versionRepository.getByType(VersionType.CITY);

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("bus_type"))
                    .isEqualTo(busType.name().toLowerCase());
                softly.assertThat((Long)response.body().jsonPath().getLong("now_bus.bus_number")).isEqualTo(400);
                softly.assertThat((Long)response.body().jsonPath().getLong("now_bus.remain_time"))
                    .isEqualTo(
                        BusRemainTime.from(600L, version.getUpdatedAt().toLocalTime()).getRemainSeconds(clock));
                softly.assertThat((Long)response.body().jsonPath().getLong("next_bus.bus_number")).isEqualTo(405);
                softly.assertThat((Long)response.body().jsonPath().getLong("next_bus.remain_time"))
                    .isEqualTo(
                        BusRemainTime.from(800L, version.getUpdatedAt().toLocalTime()).getRemainSeconds(clock));
            }
        );
    }
}
