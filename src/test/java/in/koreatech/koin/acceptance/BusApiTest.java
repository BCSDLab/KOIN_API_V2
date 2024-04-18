package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.version.model.VersionType.CITY;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.bus.dto.BusTimetableResponse;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.city.CityBusArrival;
import in.koreatech.koin.domain.bus.model.city.CityBusCache;
import in.koreatech.koin.domain.bus.model.city.CityBusCacheInfo;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRoute;
import in.koreatech.koin.domain.bus.model.mongo.BusCourse;
import in.koreatech.koin.domain.bus.model.mongo.Route;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.domain.version.service.VersionService;
import in.koreatech.koin.support.JsonAssertions;
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

    @Autowired
    private ExpressBusCacheRepository expressBusCacheRepository;

    @Autowired
    private VersionService versionService;

    private final Instant UPDATED_AT = ZonedDateTime.parse(
            "2024-02-21 18:00:00 KST",
            ofPattern("yyyy-MM-dd " + "HH:mm:ss z")
        )
        .toInstant();

    @BeforeEach
    void setup() {
        when(clock.instant()).thenReturn(UPDATED_AT);
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());
        handler.setDateTimeProvider(dateTimeProvider);

        final String arrivalTime = "18:10";
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
    }

    @AfterEach
    void end() {
        handler.setDateTimeProvider(null);
    }

    @Test
    @DisplayName("다음 셔틀버스까지 남은 시간을 조회한다.")
    void getNextShuttleBusRemainTime() {
        final String arrivalTime = "18:10";

        BusType busType = BusType.from("shuttle");
        BusStation depart = BusStation.from("koreatech");
        BusStation arrival = BusStation.from("terminal");

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
                softly.assertThat((Long) response.body().jsonPath().get("now_bus.bus_number")).isNull();
                softly.assertThat(response.body().jsonPath().getLong("now_bus.remain_time")).isEqualTo(
                    BusRemainTime.from(arrivalTime).getRemainSeconds(clock));
                softly.assertThat((Long) response.body().jsonPath().get("next_bus.bus_number")).isNull();
                softly.assertThat((Long) response.body().jsonPath().get("next_bus.remain_time")).isNull();
            }
        );
    }

    @Test
    @DisplayName("다음 시내버스까지 남은 시간을 조회한다. - Redis")
    void getNextCityBusRemainTimeRedis() {
        final long remainTime = 600L;
        final long busNumber = 400;

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(UPDATED_AT));

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

        Instant requestedAt = ZonedDateTime.parse("2024-02-21 18:00:30 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z"))
            .toInstant();

        when(clock.instant()).thenReturn(requestedAt);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(requestedAt));

        cityBusCacheRepository.save(
            CityBusCache.of(
                depart.getNodeId(direction),
                List.of(CityBusCacheInfo.of(
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

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("bus_type"))
                    .isEqualTo(busType.name().toLowerCase());
                softly.assertThat((Long) response.body().jsonPath().getLong("now_bus.bus_number")).isEqualTo(busNumber);
                softly.assertThat((Long) response.body().jsonPath().getLong("now_bus.remain_time"))
                    .isEqualTo(
                        BusRemainTime.of(remainTime, version.getUpdatedAt().toLocalTime()).getRemainSeconds(clock));
                softly.assertThat(response.body().jsonPath().getObject("next_bus.bus_number", Long.class)).isNull();
                softly.assertThat(response.body().jsonPath().getObject("next_bus.remain_time", Long.class)).isNull();
            }
        );
    }

    @Test
    @DisplayName("다음 시내버스까지 남은 시간을 조회한다. - OpenApi")
    void getNextCityBusRemainTimeOpenApi() {
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(UPDATED_AT));

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

        Instant requestedAt = ZonedDateTime.parse("2024-02-21 21:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z"))
            .toInstant();

        when(clock.instant()).thenReturn(requestedAt);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(requestedAt));

        String busApiReturnValue = """
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
                      },
                      {
                        "arrprevstationcnt": 10,
                        "arrtime": 700,
                        "nodeid": "CAB285000686",
                        "nodenm": "종합터미널",
                        "routeid": "CAB285000024",
                        "routeno": 200,
                        "routetp": "일반버스",
                        "vehicletp": "일반차량"
                      }
                    ]
                  },
                  "numOfRows": 30,
                  "pageNo": 1,
                  "totalCount": 3
                }
              }
            }
            """;

        String nodeId = depart.getNodeId(direction);
        when(cityBusOpenApiClient.getOpenApiResponse(nodeId)).thenReturn(busApiReturnValue);

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

        Version version = versionRepository.getByType(CITY);

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("bus_type"))
                    .isEqualTo(busType.name().toLowerCase());
                softly.assertThat((Long) response.body().jsonPath().getLong("now_bus.bus_number")).isEqualTo(400);
                softly.assertThat((Long) response.body().jsonPath().getLong("now_bus.remain_time"))
                    .isEqualTo(
                        BusRemainTime.of(600L, version.getUpdatedAt().toLocalTime()).getRemainSeconds(clock));
                softly.assertThat((Long) response.body().jsonPath().getLong("next_bus.bus_number")).isEqualTo(405);
                softly.assertThat((Long) response.body().jsonPath().getLong("next_bus.remain_time"))
                    .isEqualTo(
                        BusRemainTime.of(800L, version.getUpdatedAt().toLocalTime()).getRemainSeconds(clock));
            }
        );
    }

    @Test
    @DisplayName("셔틀버스의 코스 정보들을 조회한다.")
    void getBusCourses() {
        var response = RestAssured
            .given()
            .when()
            .get("/bus/courses")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getList("").size()).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getString("[0].bus_type")).isEqualTo("shuttle");
                softly.assertThat(response.body().jsonPath().getString("[0].direction")).isEqualTo("from");
                softly.assertThat(response.body().jsonPath().getString("[0].region")).isEqualTo("천안");
            }
        );
    }

    @Test
    @DisplayName("다음 셔틀버스까지 남은 시간을 조회한다.")
    void getSearchTimetable() {
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(UPDATED_AT));
        versionRepository.save(
            Version.builder()
                .version("20240_1711255839")
                .type("express_bus_timetable")
                .build()
        );

        ZonedDateTime requestedAt = ZonedDateTime.parse("2024-02-21 18:05:00 KST",
            ofPattern("yyyy-MM-dd " + "HH:mm:ss z"));

        when(clock.instant()).thenReturn(requestedAt.toInstant());
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(requestedAt.toInstant()));

        final String arrivalTime = "18:10";

        BusStation depart = BusStation.from("koreatech");
        BusStation arrival = BusStation.from("terminal");

        ExpressBusCache expressBusCache = ExpressBusCache.of(
            new ExpressBusRoute(depart.name().toLowerCase(), arrival.name().toLowerCase()),
            List.of(
                new ExpressBusCacheInfo(
                    LocalTime.parse(arrivalTime),
                    LocalTime.parse("18:55"),
                    5000
                ),
                new ExpressBusCacheInfo(
                    LocalTime.parse(arrivalTime).plusSeconds(10),
                    LocalTime.parse("18:55"),
                    5000
                ),
                new ExpressBusCacheInfo(
                    LocalTime.parse(arrivalTime).plusSeconds(5),
                    LocalTime.parse("18:55"),
                    5000
                )
            )
        );
        expressBusCacheRepository.save(expressBusCache);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("date", requestedAt.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .param("time", requestedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
            .param("depart", depart.name())
            .param("arrival", arrival.name())
            .get("/bus/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getList("", SingleBusTimeResponse.class))
                    .containsExactly(
                        new SingleBusTimeResponse("express", LocalTime.parse(arrivalTime)),
                        new SingleBusTimeResponse("shuttle", LocalTime.parse(arrivalTime)),
                        new SingleBusTimeResponse("commuting", null)
                    );
            }
        );
    }

    @Test
    @DisplayName("시내버스 시간표를 조회한다 - 지원하지 않음")
    void getCityBusTimetable() {
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(UPDATED_AT));
        Instant requestedAt = ZonedDateTime.parse("2024-02-21 21:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z"))
            .toInstant();

        when(clock.instant()).thenReturn(requestedAt);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(requestedAt));

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("bus_type", "city")
            .param("direction", "to")
            .param("region", "천안")
            .get("/bus/timetable")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("셔틀버스 시간표를 조회한다.")
    void getShuttleBusTimetable() {
        BusType busType = BusType.from("shuttle");
        String direction = "from";
        String region = "천안";

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("bus_type", busType.name().toLowerCase())
            .param("direction", direction)
            .param("region", region)
            .get("/bus/timetable")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "routeName": "주중",
                        "arrivalInfo": [
                            {
                                "nodeName": "한기대",
                                "arrivalTime": "18:10"
                            },
                            {
                                "nodeName": "신계초,운전리,연춘리",
                                "arrivalTime": "정차"
                            },
                            {
                                "nodeName": "천안역(학화호두과자)",
                                "arrivalTime": "18:50"
                            },{
                                "nodeName": "터미널(신세계 앞 횡단보도)",
                                "arrivalTime": "18:55"
                            }
                        ]
                    }
                ]
                """);
    }

    @Test
    @DisplayName("셔틀버스 시간표를 조회한다(업데이트 시각 포함).")
    void getShuttleBusTimetableWithUpdatedAt() {
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(UPDATED_AT));

        versionRepository.save(
            Version.builder()
                .version("20240_1712920946")
                .type("shuttle_bus_timetable")
                .build()
        );

        BusType busType = BusType.from("shuttle");
        String direction = "from";
        String region = "천안";

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("bus_type", busType.name().toLowerCase())
            .param("direction", direction)
            .param("region", region)
            .get("/bus/timetable/v2")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "bus_timetable": [
                        {
                            "route_name": "주중",
                            "arrival_info": [
                                {
                                    "nodeName": "한기대",
                                    "arrivalTime": "18:10"
                                },
                                {
                                    "nodeName": "신계초,운전리,연춘리",
                                    "arrivalTime": "정차"
                                },
                                {
                                    "nodeName": "천안역(학화호두과자)",
                                    "arrivalTime": "18:50"
                                },{
                                    "nodeName": "터미널(신세계 앞 횡단보도)",
                                    "arrivalTime": "18:55"
                                }
                            ]
                        }
                    ],
                    "updated_at": "2024-02-21T18:00:00"
                }
                """);
    }
}
