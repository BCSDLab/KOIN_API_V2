package in.koreatech.koin.acceptance.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.enums.BusRouteType;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
import in.koreatech.koin.domain.bus.service.BusNoticeRepository;
import in.koreatech.koin.domain.bus.service.BusService;
import in.koreatech.koin.domain.bus.service.city.CityBusService;
import in.koreatech.koin.domain.bus.service.express.ExpressBusService;
import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import in.koreatech.koin.domain.bus.service.model.route.ShuttleBusRouteStrategy;
import in.koreatech.koin.domain.bus.service.shuttle.ShuttleBusRepository;
import in.koreatech.koin.domain.bus.service.shuttle.ShuttleBusService;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.domain.version.service.VersionService;

@Testcontainers
@SpringBootTest(classes = ShuttleBusQueryMongoIntegrationTest.TestApplication.class)
class ShuttleBusQueryMongoIntegrationTest {

    private static final String COLLECTION = "shuttlebus_timetables";
    private static final String FIXTURE = "fixtures/shuttle/regular-visible-corrected.json";
    private static final String SEMESTER = "정규학기";
    private static final LocalDate SATURDAY = LocalDate.of(2026, 9, 26);
    private static final LocalDate SUNDAY = LocalDate.of(2026, 9, 27);
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 9, 23);
    private static final LocalDate THURSDAY = LocalDate.of(2026, 9, 24);
    private static final Clock FIXED_CLOCK = Clock.fixed(
        LocalDate.of(2026, 9, 26).atTime(7, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
        ZoneId.of("Asia/Seoul")
    );
    @Container
    static final GenericContainer<?> MONGO = new GenericContainer<>(DockerImageName.parse("mongo:6.0.14"))
        .withExposedPorts(27017);

    @MockBean
    private VersionRepository versionRepository;

    @MockBean
    private VersionService versionService;

    @MockBean
    private BusNoticeRepository busNoticeRepository;

    @MockBean
    private ExpressBusService expressBusService;

    @MockBean
    private CityBusService cityBusService;

    private final MongoTemplate mongoTemplate;
    private final ShuttleBusRepository shuttleBusRepository;
    private final BusService busService;
    private final ShuttleBusService shuttleBusService;
    private Map<String, Document> rawDocumentsBeforeQuery;

    @org.springframework.beans.factory.annotation.Autowired
    ShuttleBusQueryMongoIntegrationTest(
        MongoTemplate mongoTemplate,
        ShuttleBusRepository shuttleBusRepository,
        BusService busService,
        ShuttleBusService shuttleBusService
    ) {
        this.mongoTemplate = mongoTemplate;
        this.shuttleBusRepository = shuttleBusRepository;
        this.busService = busService;
        this.shuttleBusService = shuttleBusService;
    }

    @DynamicPropertySource
    static void configureMongoProperties(DynamicPropertyRegistry registry) {
        String mongoHost = MONGO.getHost();
        if (!List.of("localhost", "127.0.0.1", "::1").contains(mongoHost)) {
            throw new IllegalStateException("Refusing non-loopback Mongo host: " + mongoHost);
        }

        registry.add(
            "spring.data.mongodb.uri",
            () -> "mongodb://" + mongoHost + ":" + MONGO.getMappedPort(27017)
                + "/shuttle_query_integration"
        );
        registry.add("server.address", () -> "127.0.0.1");
    }

    @BeforeEach
    void seedRegularVisibleFixture() throws IOException {
        doReturn(Version.builder()
            .type(VersionType.SHUTTLE.getValue())
            .version("test_version")
            .title(SEMESTER)
            .content("test")
            .build()
        ).when(versionRepository).getByTypeAndIsPrevious(VersionType.SHUTTLE, false);

        Version version = Version.builder()
            .type(VersionType.SHUTTLE.getValue())
            .version("test_version")
            .title(SEMESTER)
            .content("test")
            .build();
        doReturn(version).when(versionService).getVersionEntity(VersionType.SHUTTLE);

        mongoTemplate.getCollection(COLLECTION).deleteMany(new Document());
        List<Document> fixture = loadFixture();
        mongoTemplate.getCollection(COLLECTION).insertMany(fixture);
        assertThat(mongoTemplate.getCollection(COLLECTION).countDocuments()).isEqualTo(23);
        rawDocumentsBeforeQuery = rawDocumentSnapshot();
    }

    @AfterEach
    void assertQueriesDidNotWriteRawMongoDocuments() {
        if (rawDocumentsBeforeQuery != null) {
            assertThat(rawDocumentSnapshot())
                .usingRecursiveComparison()
                .isEqualTo(rawDocumentsBeforeQuery);
        }
    }

    @Test
    @DisplayName("실제 Mongo aggregation projection으로 토요일과 일요일의 순환 시간표를 조회한다")
    void actualMongoProjectionReturnsWeekendSchedules() {
        assertThat(shuttleBusRepository.findBySemesterType(SEMESTER, "SAT")).hasSize(15);

        List<ScheduleInfo> saturday = schedule(BusStation.TERMINAL, BusStation.KOREATECH, SATURDAY);
        assertRouteTimes(saturday, "일학습병행대학 천안시내", "08:05", "08:15", "10:15");
        assertRouteTimes(saturday, "대학원", "08:00");
        assertRouteTimes(saturday, "천안 셔틀", "14:25", "18:30");
        assertThat(saturday)
            .noneMatch(schedule -> schedule.busName().equals("전문대학원"));

        List<ScheduleInfo> sunday = schedule(BusStation.TERMINAL, BusStation.KOREATECH, SUNDAY);
        assertRouteTimes(sunday, "천안 셔틀", "15:30", "17:25", "21:15", "21:30");
    }

    @Test
    @DisplayName("실제 Mongo projection은 정류장과 시간 배열 길이가 다르면 잘라서 노출하지 않는다")
    void excludesMalformedRouteWithExtraArrivalTime() {
        Document malformedRoute = new Document("_id", new ObjectId("6a941f37c9bf31464a2698ff"))
            .append("semester_type", SEMESTER)
            .append("region", "CHEONAN_ASAN")
            .append("route_type", "SHUTTLE")
            .append("route_name", "malformed-extra-arrival-time")
            .append("node_info", List.of(
                new Document("name", "터미널"),
                new Document("name", "한기대")
            ))
            .append("route_info", List.of(
                new Document("name", "malformed")
                    .append("running_days", List.of("SAT"))
                    .append("arrival_time", List.of("07:30", "07:40", "08:50"))
            ));
        mongoTemplate.getCollection(COLLECTION).insertOne(malformedRoute);
        rawDocumentsBeforeQuery = rawDocumentSnapshot();

        var malformedProjection = shuttleBusRepository.findBySemesterType(SEMESTER, "SAT").stream()
            .filter(route -> route.getRouteName().equals("malformed-extra-arrival-time"))
            .findFirst()
            .orElseThrow();
        assertThat(malformedProjection.isArrayLengthsMatch()).isFalse();

        assertThat(schedule(BusStation.TERMINAL, BusStation.KOREATECH, SATURDAY))
            .noneMatch(schedule -> schedule.busName().equals("malformed-extra-arrival-time"));

        List<LocalTime> remainTimes = shuttleBusService.getShuttleBusRemainTimes(
            BusType.SHUTTLE,
            BusStation.TERMINAL,
            BusStation.KOREATECH
        ).stream().map(BusRemainTime::getBusArrivalTime).toList();
        assertThat(remainTimes).doesNotContain(LocalTime.of(7, 30));

        List<LocalTime> searchTimes = busService.searchTimetable(
                SATURDAY,
                LocalTime.of(7, 0),
                BusStation.TERMINAL,
                BusStation.KOREATECH
            ).stream()
            .filter(response -> response.busName().equals("shuttle"))
            .map(SingleBusTimeResponse::busTime)
            .toList();
        assertThat(searchTimes).containsExactly(LocalTime.of(8, 0));
    }

    @Test
    @DisplayName("목요일 천안역에서 학교로 가는 조회에서 복귀 없는 추가 3회차를 제외한다")
    void excludesThursdayExtraRoundWithoutSchoolArrival() {
        List<ScheduleInfo> schedules = schedule(BusStation.STATION, BusStation.KOREATECH, THURSDAY);

        assertThat(schedules)
            .noneMatch(schedule -> schedule.busName().equals("천안 셔틀")
                && schedule.departTime().equals(LocalTime.of(17, 0)));
    }

    @Test
    @DisplayName("토요일 학교에서 터미널과 천안역으로 가는 대학원 및 A/B 정류장을 조회한다")
    void supportsSaturdayReverseGraduateAndStationAliases() {
        List<ScheduleInfo> schoolToTerminal = schedule(BusStation.KOREATECH, BusStation.TERMINAL, SATURDAY);
        assertRouteTimes(schoolToTerminal, "일학습병행대학 천안시내", "19:10", "19:20");
        assertRouteTimes(schoolToTerminal, "대학원", "18:10");

        List<ScheduleInfo> schoolToStation = schedule(BusStation.KOREATECH, BusStation.STATION, SATURDAY);
        assertRouteTimes(schoolToStation, "일학습병행대학 천안시내", "19:10", "19:20");

        List<ScheduleInfo> stationToTerminal = schedule(BusStation.STATION, BusStation.TERMINAL, SATURDAY);
        assertRouteTimes(stationToTerminal, "전문대학원", "08:45", "13:00");
    }

    @Test
    @DisplayName("수요일 통학 노선은 대학 별칭과 역방향 학교 출발 시각을 실제 Mongo에서 조회한다")
    void supportsWeekdaySchoolAliasAndReverseDirection() {
        List<ScheduleInfo> stationToSchool = schedule(BusStation.STATION, BusStation.KOREATECH, WEDNESDAY);
        assertRouteTimes(stationToSchool, "천안역", "08:10");

        List<ScheduleInfo> schoolToStation = schedule(BusStation.KOREATECH, BusStation.STATION, WEDNESDAY);
        assertRouteTimes(schoolToStation, "천안역", "18:10");
    }

    @Test
    @DisplayName("고정 시각 토요일 07시의 search와 remain이 같은 미래 첫 운행을 반환하고 null marker를 건너뛴다")
    void searchAndRemainShareFixedSaturdayFutureOccurrences() {
        List<BusRemainTime> remainTimes = shuttleBusService.getShuttleBusRemainTimes(
            BusType.SHUTTLE,
            BusStation.TERMINAL,
            BusStation.KOREATECH
        );
        List<LocalTime> actualRemainTimes = remainTimes.stream()
            .map(BusRemainTime::getBusArrivalTime)
            .toList();

        assertThat(actualRemainTimes)
            .containsExactly(
                LocalTime.of(8, 0),
                LocalTime.of(8, 5),
                LocalTime.of(8, 15),
                LocalTime.of(10, 15),
                LocalTime.of(14, 25),
                LocalTime.of(18, 30)
            );

        List<SingleBusTimeResponse> searchTimes = busService.searchTimetable(
            SATURDAY,
            LocalTime.of(7, 0),
            BusStation.TERMINAL,
            BusStation.KOREATECH
        );

        assertThat(searchTimes)
            .containsExactly(
                new SingleBusTimeResponse("shuttle", LocalTime.of(8, 0)),
                new SingleBusTimeResponse("commuting", null)
            );
        assertThat(searchTimes.stream()
            .filter(response -> response.busName().equals("shuttle"))
            .findFirst()
            .orElseThrow()
            .busTime()
        ).isEqualTo(actualRemainTimes.get(0));
    }

    private List<ScheduleInfo> schedule(BusStation depart, BusStation arrival, LocalDate date) {
        return busService.getBusSchedule(new BusRouteCommand(
            depart,
            arrival,
            BusRouteType.SHUTTLE,
            date,
            LocalTime.MIDNIGHT
        )).schedule();
    }

    private void assertRouteTimes(List<ScheduleInfo> schedules, String routeName, String... expectedTimes) {
        List<LocalTime> expected = Arrays.stream(expectedTimes)
            .map(LocalTime::parse)
            .toList();
        assertThat(schedules.stream()
            .filter(schedule -> schedule.busName().equals(routeName))
            .map(ScheduleInfo::departTime)
            .toList()
        ).as(routeName).containsExactlyInAnyOrderElementsOf(expected);
    }

    private List<Document> loadFixture() throws IOException {
        try (InputStream inputStream = Objects.requireNonNull(
            getClass().getClassLoader().getResourceAsStream(FIXTURE))) {
            JsonNode root = new ObjectMapper().readTree(inputStream);
            List<Document> documents = new ArrayList<>();
            root.forEach(node -> documents.add(Document.parse(node.toString())));
            return documents;
        }
    }

    private Map<String, Document> rawDocumentSnapshot() {
        return mongoTemplate.getCollection(COLLECTION).find().into(new ArrayList<>()).stream()
            .collect(Collectors.toMap(
                document -> document.getObjectId("_id").toHexString(),
                document -> Document.parse(document.toJson())
            ));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
    })
    @EnableMongoRepositories(basePackageClasses = ShuttleBusRepository.class)
    @Import({
        BusService.class,
        ShuttleBusService.class,
        ShuttleBusRouteStrategy.class
    })
    static class TestApplication {

        @Bean
        Clock clock() {
            return FIXED_CLOCK;
        }
    }
}
