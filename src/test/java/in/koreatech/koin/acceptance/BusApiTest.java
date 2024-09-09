package in.koreatech.koin.acceptance;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

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

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BusApiTest extends AcceptanceTest {

    @Autowired
    private BusFixture busFixture;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private CityBusCacheRepository cityBusCacheRepository;

    @Autowired
    private ExpressBusCacheRepository expressBusCacheRepository;

    @BeforeAll
    void setup() {
        clear();
        busFixture.버스_시간표_등록();
        busFixture.시내버스_시간표_등록();
    }

    @Test
    void 다음_셔틀버스까지_남은_시간을_조회한다() throws Exception {

        mockMvc.perform(
                get("/bus")
                    .param("bus_type", "shuttle")
                    .param("depart", "koreatech")
                    .param("arrival", "terminal")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "bus_type": "shuttle",
                    "now_bus": {
                        "bus_number": null,
                        "remain_time": 22200
                    },
                    "next_bus": null
                }
                """));
    }

    @Test
    void 다음_시내버스까지_남은_시간을_조회한다_Redis_캐시_히트() throws Exception {
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

        mockMvc.perform(
                get("/bus")
                    .param("bus_type", busType.getName())
                    .param("depart", depart.name())
                    .param("arrival", arrival.name())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "bus_type": "city",
                    "now_bus": {
                        "bus_number": 400,
                        "remain_time": 600
                    },
                    "next_bus": null
                }
                """));
    }

    @Test
    void 셔틀버스의_코스_정보들을_조회한다() throws Exception {
        mockMvc.perform(
                get("/bus/courses")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].bus_type").value("shuttle"))
            .andExpect(jsonPath("$[0].direction").value("from"))
            .andExpect(jsonPath("$[0].region").value("천안"));
    }

    @Test
    void 다음_셔틀버스까지_남은_시간을_조회한다2() throws Exception {
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

        MvcResult result = mockMvc.perform(
                get("/bus/search")
                    .param("date", requestedAt.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .param("time", requestedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .param("depart", depart.name())
                    .param("arrival", arrival.name())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        SoftAssertions.assertSoftly(
            softly -> {
                JsonNode jsonNode = JsonAssertions.convertJsonNode(result);
                List<SingleBusTimeResponse> actualResponseList = JsonAssertions.convertToList(jsonNode, SingleBusTimeResponse.class);
                softly.assertThat(actualResponseList)
                    .containsExactly(
                        new SingleBusTimeResponse("express", LocalTime.parse(arrivalTime)),
                        new SingleBusTimeResponse("shuttle", LocalTime.parse(arrivalTime)),
                        new SingleBusTimeResponse("commuting", null)
                    );
            }
        );
    }

    @Test
    void 시내버스_시간표를_조회한다_지원하지_않음() throws Exception {
        Version version = Version.builder()
            .version("test_version")
            .type(VersionType.CITY.getValue())
            .build();
        versionRepository.save(version);

        Long busNumber = 400L;
        String direction = "종합터미널";

        mockMvc.perform(
                get("/bus/timetable/city")
                    .param("bus_number", String.valueOf(busNumber))
                    .param("direction", direction)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
            """));
    }

    @Test
    void 셔틀버스_시간표를_조회한다() throws Exception {
        mockMvc.perform(
                get("/bus/timetable")
                    .param("bus_type", "shuttle")
                    .param("direction", "from")
                    .param("region", "천안")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
            """));
    }

    @Test
    void 셔틀버스_시간표를_조회한다_업데이트_시각_포함() throws Exception {
        Version version = Version.builder()
            .version("test_version")
            .type(VersionType.SHUTTLE.getValue())
            .build();
        versionRepository.save(version);

        BusType busType = BusType.from("shuttle");
        String direction = "from";
        String region = "천안";

        mockMvc.perform(
                get("/bus/timetable/v2")
                    .param("bus_type", busType.getName())
                    .param("direction", direction)
                    .param("region", region)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
            """));
    }
}
