package in.koreatech.koin.acceptance;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.model.city.CityBusArrival;
import in.koreatech.koin.domain.bus.model.city.CityBusCache;
import in.koreatech.koin.domain.bus.model.city.CityBusCacheInfo;
import in.koreatech.koin.domain.bus.model.enums.BusDirection;
import in.koreatech.koin.domain.bus.model.enums.BusStation;
import in.koreatech.koin.domain.bus.model.enums.BusType;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRoute;
import in.koreatech.koin.domain.bus.repository.CityBusCacheRepository;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.fixture.BusFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
class BusApiTest extends AcceptanceTest {

    @Autowired
    private BusFixture busFixture;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private CityBusCacheRepository cityBusCacheRepository;

    @Autowired
    private ExpressBusCacheRepository expressBusCacheRepository;

    @BeforeEach
    void setup() {
        busFixture.버스_시간표_등록();
        busFixture.시내버스_시간표_등록();
    }

    @Test
    @DisplayName("다음 셔틀버스까지 남은 시간을 조회한다.")
    void getNextShuttleBusRemainTime() {
        var response = RestAssured
            .given()
            .when()
            .param("bus_type", "shuttle")
            .param("depart", "koreatech")
            .param("arrival", "terminal")
            .get("/bus")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "bus_type": "shuttle",
                    "now_bus": {
                        "bus_number": null,
                        "remain_time": 22200
                    },
                    "next_bus": null
                }
                """);
    }

    @Test
    @DisplayName("다음 시내버스까지 남은 시간을 조회한다. - Redis 캐시 히트")
    void getNextCityBusRemainTimeRedis() {
        final long remainTime = 600L;
        final long busNumber = 400;
        BusType busType = BusType.CITY;
        BusStation depart = BusStation.TERMINAL;
        BusStation arrival = BusStation.KOREATECH;

        BusDirection direction = BusStation.getDirection(depart, arrival);
        Version version = versionRepository.save(
            Version.builder()
                .version("test_version")
                .type(VersionType.CITY.getValue())
                .build()
        );

        cityBusCacheRepository.save(
            CityBusCache.of(
                depart.getNodeId(direction).get(0),
                List.of(CityBusCacheInfo.of(
                    CityBusArrival.builder()
                        .routeno(busNumber)
                        .arrtime(remainTime)
                        .build(),
                    version.getUpdatedAt())
                )
            )
        );

        var response = RestAssured
            .given()
            .when()
            .param("bus_type", busType.getName())
            .param("depart", depart.name())
            .param("arrival", arrival.name())
            .get("/bus")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "bus_type": "city",
                    "now_bus": {
                        "bus_number": 400,
                        "remain_time": 600
                    },
                    "next_bus": null
                }
                """);
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
        versionRepository.save(
            Version.builder()
                .version("test_version")
                .type(VersionType.EXPRESS.getValue())
                .build()
        );

        ZonedDateTime requestedAt = ZonedDateTime.parse("2024-01-15 12:05:00 KST",
            ofPattern("yyyy-MM-dd " + "HH:mm:ss z"));

        final String arrivalTime = "18:10";

        BusStation depart = BusStation.from("koreatech");
        BusStation arrival = BusStation.from("terminal");

        ExpressBusCache expressBusCache = ExpressBusCache.of(
            new ExpressBusRoute(depart.getName(), arrival.getName()),
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

        var response = RestAssured
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
        Version version = Version.builder()
            .version("test_version")
            .type(VersionType.CITY.getValue())
            .build();
        versionRepository.save(version);

        Long busNumber = 400L;
        String direction = "종합터미널";

        var response = RestAssured
            .given()
            .when()
            .param("bus_number", busNumber)
            .param("direction", direction)
            .get("/bus/timetable/city")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "bus_info": {
                        "arrival_node": "종합터미널",
                        "depart_node": "병천3리",
                        "number": 400
                    },
                    "bus_timetables": [
                        {
                            "day_of_week": "평일",
                            "depart_info": ["06:00", "07:00"]
                        },
                        {
                            "day_of_week": "주말",
                            "depart_info": ["08:00", "09:00"]
                        }
                    ],
                    "updated_at": "2024-07-19 19:00:00"
                }
                """);
    }

    @Test
    @DisplayName("셔틀버스 시간표를 조회한다.")
    void getShuttleBusTimetable() {
        var response = RestAssured
            .given()
            .when()
            .param("bus_type", "shuttle")
            .param("direction", "from")
            .param("region", "천안")
            .get("/bus/timetable")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "route_name": "주중",
                        "arrival_info": [
                            {
                                "node_name": "한기대",
                                "arrival_time": "18:10"
                            },
                            {
                                "node_name": "신계초,운전리,연춘리",
                                "arrival_time": "정차"
                            },
                            {
                                "node_name": "천안역(학화호두과자)",
                                "arrival_time": "18:50"
                            },
                            {
                                "node_name": "터미널(신세계 앞 횡단보도)",
                                "arrival_time": "18:55"
                            }
                        ]
                    }
                ]
                """);
    }

    @Test
    @DisplayName("셔틀버스 시간표를 조회한다(업데이트 시각 포함).")
    void getShuttleBusTimetableWithUpdatedAt() {
        Version version = Version.builder()
            .version("test_version")
            .type(VersionType.SHUTTLE.getValue())
            .build();
        versionRepository.save(version);

        BusType busType = BusType.from("shuttle");
        String direction = "from";
        String region = "천안";

        var response = RestAssured
            .given()
            .when()
            .param("bus_type", busType.getName())
            .param("direction", direction)
            .param("region", region)
            .get("/bus/timetable/v2")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "bus_timetables": [
                        {
                            "route_name": "주중",
                            "arrival_info": [
                                {
                                    "node_name": "한기대",
                                    "arrival_time": "18:10"
                                },
                                {
                                    "node_name": "신계초,운전리,연춘리",
                                    "arrival_time": "정차"
                                },
                                {
                                    "node_name": "천안역(학화호두과자)",
                                    "arrival_time": "18:50"
                                },
                                {
                                    "node_name": "터미널(신세계 앞 횡단보도)",
                                    "arrival_time": "18:55"
                                }
                            ]
                        }
                    ],
                    "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }
}
