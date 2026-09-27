package in.koreatech.koin.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import in.koreatech.koin.admin.bus.commuting.controller.AdminCommutingBusController;
import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.commuting.extractor.AdminCommutingBusDateExtractor;
import in.koreatech.koin.admin.bus.commuting.extractor.AdminCommutingBusExcelMetaDataExtractor;
import in.koreatech.koin.admin.bus.commuting.extractor.AdminCommutingBusNodeInfoRowIndexExtractor;
import in.koreatech.koin.admin.bus.commuting.extractor.AdminCommutingBusRouteInfoExtractor;
import in.koreatech.koin.admin.bus.commuting.repository.AdminCommutingBusRepository;
import in.koreatech.koin.admin.bus.commuting.service.AdminCommutingBusExcelService;
import in.koreatech.koin.admin.bus.commuting.service.AdminCommutingBusService;
import in.koreatech.koin.admin.bus.shuttle.controller.AdminShuttleBusTimetableController;
import in.koreatech.koin.admin.bus.shuttle.repository.AdminShuttleBusTimetableRepository;
import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusExcelService;
import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusService;
import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.exception.GlobalExceptionHandler;

@Testcontainers
@SpringBootTest(
    classes = AdminShuttleBusTimetableMongoIntegrationTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class AdminShuttleBusTimetableMongoIntegrationTest {

    private static final String COLLECTION = "shuttlebus_timetables";
    private static final String FIXTURE = "fixtures/shuttle/regular-cheonan-timetable-damaged.json";
    private static final String ADMIN_TIMETABLES_FIXTURE = "fixtures/shuttle/admin-timetables.json";
    private static final String CORRECTED_EXPORT_FIXTURE = "fixtures/shuttle/regular-visible-corrected.json";
    private static final ObjectId TIMETABLE_ID = new ObjectId("6a941f37c9bf31464a269865");
    private static final ObjectId CITY_REPLACEMENT_ID = new ObjectId("6a941f37c9bf31464a269867");
    private static final ObjectId FIRST_COMMUTING_ID = new ObjectId("6a941f37c9bf31464a269854");
    private static final ObjectId SECOND_COMMUTING_ID = new ObjectId("6a941f37c9bf31464a269855");

    private static final List<String> CORRECT_SATURDAY_SECOND =
        Arrays.asList(null, null, "18:30", "18:35", "19:15");
    private static final List<String> CORRECT_SUNDAY_SECOND =
        Arrays.asList("17:00", null, "17:25", "17:30", "18:10");

    @Container
    static final GenericContainer<?> MONGO = new GenericContainer<>(DockerImageName.parse("mongo:6.0.14"))
        .withExposedPorts(27017);

    private final ObjectMapper objectMapper;
    private final TestRestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;

    @Autowired
    AdminShuttleBusTimetableMongoIntegrationTest(
        ObjectMapper objectMapper,
        TestRestTemplate restTemplate,
        MongoTemplate mongoTemplate
    ) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @DynamicPropertySource
    static void configureMongoProperties(DynamicPropertyRegistry registry) {
        String mongoHost = MONGO.getHost();
        if (!List.of("localhost", "127.0.0.1", "::1").contains(mongoHost)) {
            throw new IllegalStateException("Refusing non-loopback Mongo host: " + mongoHost);
        }

        registry.add(
            "spring.data.mongodb.uri",
            () -> "mongodb://" + MONGO.getHost() + ":" + MONGO.getMappedPort(27017)
                + "/shuttle_timetable_integration"
        );
        registry.add("server.address", () -> "127.0.0.1");
    }

    @BeforeEach
    void setUpDamagedRegularSemesterFixture() throws IOException {
        mongoTemplate.getCollection(COLLECTION).deleteMany(new Document());
        mongoTemplate.getCollection(COLLECTION).insertOne(loadFixture());
        assertThat(mongoTemplate.getCollection(COLLECTION).countDocuments()).isEqualTo(1);
        assertThat(mongoTemplate.getCollection(COLLECTION)
            .find(Filters.eq("_id", TIMETABLE_ID)).first().getObjectId("_id"))
            .isEqualTo(TIMETABLE_ID);

        // 운영/스테이지 쓰기 대신, 로컬 fixture만 공식 시간표 값으로 먼저 복구한다.
        mongoTemplate.getCollection(COLLECTION).updateOne(
            Filters.eq("_id", TIMETABLE_ID),
            Updates.combine(
                Updates.set("route_info.11.arrival_time", CORRECT_SATURDAY_SECOND),
                Updates.set("route_info.13.arrival_time", CORRECT_SUNDAY_SECOND)
            )
        );
    }

    @Test
    @DisplayName("정상 복구한 중복 이름 회차를 실제 PUT으로 재저장해도 각 회차 시간이 유지된다")
    void keepsSameNameRoundsSeparatedWhenUpdatingThroughHttp() throws Exception {
        assertThat(routeInfo(11).getList("arrival_time", String.class))
            .containsExactlyElementsOf(CORRECT_SATURDAY_SECOND);
        assertThat(routeInfo(13).getList("arrival_time", String.class))
            .containsExactlyElementsOf(CORRECT_SUNDAY_SECOND);
        System.out.println("before PUT route_info[11]=" + routeInfo(11).getList("arrival_time", String.class));
        System.out.println("before PUT route_info[13]=" + routeInfo(13).getList("arrival_time", String.class));

        ObjectNode fullRequest = fullRequestFromMongo();
        ResponseEntity<?> firstResponse = putTimetable(fullRequest);
        ResponseEntity<?> secondResponse = putTimetable(fullRequest);

        assertThat(firstResponse.getStatusCode()).isEqualTo(OK);
        assertThat(secondResponse.getStatusCode()).isEqualTo(OK);
        assertFullTimetableSnapshot();
        System.out.println("after PUT route_info[11]=" + routeInfo(11).getList("arrival_time", String.class));
        System.out.println("after PUT route_info[13]=" + routeInfo(13).getList("arrival_time", String.class));
    }

    @Test
    @DisplayName("반복 저장은 안정적이고 운행 요일 미전달은 보존하며 명시하면 갱신한다")
    void preservesRunningDaysAndNullPositionsAcrossRepeatedUpdates() throws Exception {
        mongoTemplate.getCollection(COLLECTION).updateOne(
            Filters.eq("_id", TIMETABLE_ID),
            Updates.set("route_info.0.detail", "하교")
        );
        ObjectNode partialRequest = baseRequest();
        ArrayNode routeInfos = objectMapper.createArrayNode();
        routeInfos.add(routeInfoRequest(
            "1회", null, Arrays.asList("12:00", null, "12:25", "12:30", "13:00")
        ));
        partialRequest.set("route_info", routeInfos);

        assertThat(putTimetable(partialRequest).getStatusCode()).isEqualTo(OK);
        Document firstRound = routeInfo(0);
        assertThat(firstRound.getList("running_days", String.class))
            .containsExactly("MON", "TUE", "WED", "THU", "FRI");
        assertThat(firstRound.getList("arrival_time", String.class))
            .containsExactly("12:00", null, "12:25", "12:30", "13:00");
        assertThat(firstRound.getString("detail")).isEqualTo("하교");

        ObjectNode explicitDaysRequest = baseRequest();
        ArrayNode explicitRouteInfos = objectMapper.createArrayNode();
        explicitRouteInfos.add(routeInfoRequest(
            "1회", List.of("SAT"), Arrays.asList("12:00", null, "12:25", "12:30", "13:00")
        ));
        explicitDaysRequest.set("route_info", explicitRouteInfos);

        assertThat(putTimetable(explicitDaysRequest).getStatusCode()).isEqualTo(OK);
        assertThat(putTimetable(explicitDaysRequest).getStatusCode()).isEqualTo(OK);

        Document repeatedRound = routeInfo(0);
        assertThat(repeatedRound.getList("running_days", String.class)).containsExactly("SAT");
        assertThat(repeatedRound.getList("arrival_time", String.class))
            .containsExactly("12:00", null, "12:25", "12:30", "13:00");
        assertThat(routeInfo(9).getList("arrival_time", String.class))
            .containsExactly("16:30", null, "16:55", "17:00", null);
        assertThat(routeInfo(13).getList("arrival_time", String.class))
            .containsExactlyElementsOf(CORRECT_SUNDAY_SECOND);
    }

    @Test
    @DisplayName("REPLACE는 포함된 문서의 순서와 중복 회차를 두 번 저장해도 그대로 재조회한다")
    void replacesIncludedDocumentSnapshotAcrossRepeatedHttpMongoReads() throws Exception {
        List<Document> original = loadAdminTimetables();
        seedTimetables(original);
        List<Document> expected = loadAdminTimetables();
        Document expectedTarget = fixtureDocument(expected, TIMETABLE_ID);

        ObjectNode replacement = exportTimetableRequest(expectedTarget);
        ArrayNode nodes = replacement.withArray("node_info");
        JsonNode firstNode = nodes.get(0).deepCopy();
        JsonNode secondNode = nodes.get(1).deepCopy();
        nodes.set(0, secondNode);
        nodes.set(1, firstNode);

        ArrayNode sourceRoutes = replacement.withArray("route_info");
        ArrayNode replacementRoutes = objectMapper.createArrayNode();
        ObjectNode renamed = sourceRoutes.get(0).deepCopy();
        renamed.put("name", "교체된 첫 회차");
        ObjectNode duplicateFirst = sourceRoutes.get(1).deepCopy();
        duplicateFirst.put("name", "교체된 중복 회차");
        ObjectNode duplicateSecond = sourceRoutes.get(2).deepCopy();
        duplicateSecond.put("name", "교체된 중복 회차");
        replacementRoutes.add(duplicateSecond).add(renamed).add(duplicateFirst);
        replacement.set("route_info", replacementRoutes);

        List<Document> expectedNodes = new ArrayList<>();
        replacement.withArray("node_info")
            .forEach(node -> expectedNodes.add(Document.parse(node.toString())));
        List<Document> expectedRoutes = new ArrayList<>();
        replacement.withArray("route_info")
            .forEach(route -> expectedRoutes.add(Document.parse(route.toString())));
        expectedTarget.put("node_info", expectedNodes);
        expectedTarget.put("route_info", expectedRoutes);

        for (int pass = 1; pass <= 2; pass++) {
            ResponseEntity<?> response = putTimetable(replacement, "REPLACE");
            assertThat(response.getStatusCode())
                .as("replace pass=%s response=%s", pass, response.getBody())
                .isEqualTo(OK);
            assertExportSnapshot(expected, "replace pass=" + pass);
        }
    }

    @Test
    @DisplayName("실제 4회차 7정류장 노선을 5회차 8정류장 snapshot으로 REPLACE하고 반복 재조회한다")
    void replacesCorrectedCitySnapshotAcrossRepeatedHttpMongoReads() throws Exception {
        List<Document> fullExport = loadAdminTimetables();
        Document originalCity = fixtureDocument(fullExport, CITY_REPLACEMENT_ID);
        Document unrelatedDocument = fullExport.stream()
            .filter(document -> !CITY_REPLACEMENT_ID.equals(document.getObjectId("_id")))
            .findFirst()
            .orElseThrow();
        assertThat(originalCity.getList("node_info", Document.class)).hasSize(7);
        assertThat(originalCity.getList("route_info", Document.class)).hasSize(4);

        List<Document> original = List.of(originalCity, unrelatedDocument);
        seedTimetables(original);

        Document correctedCity = fixtureDocument(loadCorrectedExportFixture(), CITY_REPLACEMENT_ID);
        assertThat(correctedCity.getList("node_info", Document.class)).hasSize(8);
        assertThat(correctedCity.getList("route_info", Document.class)).hasSize(5);
        ObjectNode replacement = exportTimetableRequest(correctedCity);

        List<Document> expected = List.of(correctedCity, unrelatedDocument);
        for (int pass = 1; pass <= 2; pass++) {
            ResponseEntity<?> response = putTimetable(replacement, "REPLACE");
            assertThat(response.getStatusCode())
                .as("city replacement pass=%s response=%s", pass, response.getBody())
                .isEqualTo(OK);
            assertExportSnapshot(expected, "city replacement pass=" + pass);
        }
    }

    @Test
    @DisplayName("REPLACE 뒤쪽 회차의 운행 요일이 잘못되면 앞쪽 변경도 저장하지 않는다")
    void rejectsInvalidReplacementDaysWithoutSavingEarlierDocument() throws Exception {
        List<Document> fullExport = loadAdminTimetables();
        Document originalFirst = fixtureDocument(fullExport, CITY_REPLACEMENT_ID);
        Document originalLater = fixtureDocument(fullExport, SECOND_COMMUTING_ID);
        List<Document> original = List.of(originalFirst, originalLater);
        seedTimetables(original);

        ObjectNode first = exportTimetableRequest(originalFirst);
        ((ObjectNode)first.withArray("route_info").get(0)).withArray("arrival_time")
            .set(0, objectMapper.getNodeFactory().textNode("08:01"));
        ObjectNode invalidLater = exportTimetableRequest(originalLater);
        ((ObjectNode)invalidLater.withArray("route_info").get(0)).putArray("running_days");

        ResponseEntity<String> response = putBatch("shuttle", "REPLACE", first, invalidLater);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).contains("REQUIRED_SHUTTLE_RUNNING_DAYS");
        assertExportSnapshot(original, "invalid replacement days");
    }

    @Test
    @DisplayName("REPLACE 뒤쪽 회차의 정류장별 도착 시간 개수가 다르면 앞쪽 변경도 저장하지 않는다")
    void rejectsInvalidReplacementLengthWithoutSavingEarlierDocument() throws Exception {
        List<Document> fullExport = loadAdminTimetables();
        Document originalFirst = fixtureDocument(fullExport, CITY_REPLACEMENT_ID);
        Document originalLater = fixtureDocument(fullExport, SECOND_COMMUTING_ID);
        List<Document> original = List.of(originalFirst, originalLater);
        seedTimetables(original);

        ObjectNode first = exportTimetableRequest(originalFirst);
        ((ObjectNode)first.withArray("route_info").get(0)).withArray("arrival_time")
            .set(0, objectMapper.getNodeFactory().textNode("08:01"));
        ObjectNode invalidLater = exportTimetableRequest(originalLater);
        ((ObjectNode)invalidLater.withArray("route_info").get(0)).withArray("arrival_time").remove(0);

        ResponseEntity<String> response = putBatch("shuttle", "REPLACE", first, invalidLater);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).contains("INVALID_REQUEST_BODY");
        assertExportSnapshot(original, "invalid replacement length");
    }

    @ParameterizedTest
    @ValueSource(strings = {"shuttle", "commuting"})
    @DisplayName("배치 뒤쪽의 회차 검증이 실패하면 앞 문서 변경을 포함한 전체 원본이 유지된다")
    void rejectsInvalidLaterDocumentWithoutSavingEarlierChange(String admin) throws Exception {
        List<Document> original = loadAdminTimetables();
        seedTimetables(original);
        ObjectNode first = exportTimetableRequest(fixtureDocument(original, FIRST_COMMUTING_ID));
        ((ObjectNode)first.withArray("route_info").get(0)).withArray("arrival_time")
            .set(0, objectMapper.getNodeFactory().textNode("07:01"));

        ResponseEntity<String> response = putBatch(admin, first, invalidDuplicateRequest(original, admin));

        assertThat(response.getStatusCode()).as("%s: %s", admin, response.getBody()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).contains("INVALID_REQUEST_BODY");
        assertExportSnapshot(original, admin + " invalid later document");
    }

    @ParameterizedTest
    @ValueSource(strings = {"shuttle", "commuting"})
    @DisplayName("배치 뒤쪽 검증이 실패하면 앞쪽 신규 문서도 생성하지 않는다")
    void rejectsInvalidLaterDocumentWithoutInsertingEarlierNewRoute(String admin) throws Exception {
        List<Document> original = loadAdminTimetables();
        seedTimetables(original);
        ObjectNode first = exportTimetableRequest(fixtureDocument(original, FIRST_COMMUTING_ID));
        first.put("route_name", "검증 실패 시 생성하지 않을 노선");
        first.putNull("sub_name");

        ResponseEntity<String> response = putBatch(admin, first, invalidDuplicateRequest(original, admin));

        assertThat(response.getStatusCode()).as("%s: %s", admin, response.getBody()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).contains("INVALID_REQUEST_BODY");
        assertExportSnapshot(original, admin + " no insert on invalid batch");
    }

    @ParameterizedTest
    @ValueSource(strings = {"shuttle", "commuting"})
    @DisplayName("동일 노선의 연속 부분 요청은 정규화한 키로 같은 문서에 누적한다")
    void accumulatesRepeatedPartialUpdatesForSameRoute(String admin) throws Exception {
        List<Document> original = loadAdminTimetables();
        seedTimetables(original);
        List<Document> expected = loadAdminTimetables();
        Document changed = fixtureDocument(expected, FIRST_COMMUTING_ID);
        changed.getList("route_info", Document.class).get(0).getList("arrival_time", String.class).set(0, "07:01");
        changed.getList("route_info", Document.class).get(1).getList("arrival_time", String.class).set(0, "19:01");
        ObjectNode first = partialTimetableRequest(changed, 0);
        ObjectNode second = partialTimetableRequest(changed, 1);
        second.put("region", "천안");

        ResponseEntity<String> response = putBatch(admin, first, second);

        assertThat(response.getStatusCode()).as("%s: %s", admin, response.getBody()).isEqualTo(OK);
        assertExportSnapshot(expected, admin + " accumulated partial updates");
    }

    @ParameterizedTest
    @ValueSource(strings = {"shuttle", "commuting"})
    @DisplayName("부가명 없는 동일 신규 노선을 두 번 요청해도 한 문서만 만들고 변경을 누적한다")
    void createsRepeatedNewRouteOnlyOnce(String admin) throws Exception {
        List<Document> original = loadAdminTimetables();
        seedTimetables(original);
        List<Document> expected = loadAdminTimetables();
        Document newRoute = Document.parse(fixtureDocument(original, FIRST_COMMUTING_ID).toJson());
        newRoute.remove("_id");
        newRoute.put("route_name", "동일 신규 노선 검증");
        newRoute.put("sub_name", null);
        ObjectNode first = exportTimetableRequest(newRoute);
        newRoute.getList("route_info", Document.class).get(1).getList("arrival_time", String.class).set(0, "19:01");
        ObjectNode second = partialTimetableRequest(newRoute, 1);
        second.put("region", "천안");

        ResponseEntity<String> response = putBatch(admin, first, second);

        assertThat(response.getStatusCode()).as("%s: %s", admin, response.getBody()).isEqualTo(OK);
        List<Document> created = mongoTemplate.getCollection(COLLECTION)
            .find(Filters.eq("route_name", newRoute.getString("route_name"))).into(new ArrayList<>());
        assertThat(created).hasSize(1);
        ObjectId newId = created.get(0).getObjectId("_id");
        assertThat(original).extracting(document -> document.getObjectId("_id")).doesNotContain(newId);
        newRoute.put("_id", newId);
        expected.add(newRoute);
        assertExportSnapshot(expected, admin + " repeated new route");
    }

    private Document fixtureDocument(List<Document> documents, ObjectId id) {
        return documents.stream().filter(document -> id.equals(document.getObjectId("_id")))
            .findFirst().orElseThrow();
    }

    private ObjectNode partialTimetableRequest(Document document, int roundIndex) {
        ObjectNode request = exportTimetableRequest(document);
        JsonNode round = request.withArray("route_info").get(roundIndex);
        request.set("route_info", objectMapper.createArrayNode().add(round));
        return request;
    }

    private List<Document> loadCorrectedExportFixture() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CORRECTED_EXPORT_FIXTURE)) {
            if (input == null) {
                throw new IllegalStateException("Fixture not found: " + CORRECTED_EXPORT_FIXTURE);
            }
            JsonNode exported = objectMapper.readTree(input);
            List<Document> documents = new ArrayList<>();
            for (JsonNode document : exported) {
                documents.add(Document.parse(document.toString()));
            }
            return documents;
        }
    }

    private ObjectNode invalidDuplicateRequest(List<Document> original, String admin) {
        ObjectNode request = exportTimetableRequest(fixtureDocument(original,
            "shuttle".equals(admin) ? TIMETABLE_ID : SECOND_COMMUTING_ID));
        ArrayNode rounds = request.withArray("route_info");
        if ("shuttle".equals(admin)) {
            assertThat(rounds.get(11).path("name").asText()).isEqualTo("토요일 오후");
            rounds.remove(11);
        } else {
            rounds.add(rounds.get(0).deepCopy());
        }
        return request;
    }

    private ResponseEntity<String> putBatch(String admin, ObjectNode... timetables) {
        return putBatch(admin, "PARTIAL", timetables);
    }

    private ResponseEntity<String> putBatch(String admin, String updateMode, ObjectNode... timetables) {
        ObjectNode request = objectMapper.createObjectNode();
        ArrayNode batch = request.putArray("shuttle".equals(admin) ? "shuttle_bus_timetables" : "commuting_bus_timetables");
        for (ObjectNode timetable : timetables) {
            ObjectNode copy = timetable.deepCopy();
            if ("commuting".equals(admin)) {
                copy.withArray("route_info").forEach(round -> ((ObjectNode)round).remove("running_days"));
            }
            batch.add(copy);
        }
        return putJson(
            "/admin/bus/" + admin + "/timetable?semester_type=REGULAR&update_mode=" + updateMode,
            request
        );
    }

    private List<Document> loadAdminTimetables() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(ADMIN_TIMETABLES_FIXTURE)) {
            if (input == null) {
                throw new IllegalStateException("Fixture not found: " + ADMIN_TIMETABLES_FIXTURE);
            }
            JsonNode exported = objectMapper.readTree(input);
            assertThat(exported.isArray()).isTrue();
            List<Document> documents = new ArrayList<>();
            for (JsonNode document : exported) {
                // Extended JSON의 $oid를 문자열로 바꾸지 않고 BSON ObjectId로 복원한다.
                documents.add(Document.parse(document.toString()));
            }
            assertThat(documents).hasSize(4);
            return documents;
        }
    }

    private void seedTimetables(List<Document> documents) {
        mongoTemplate.getCollection(COLLECTION).deleteMany(new Document());
        mongoTemplate.getCollection(COLLECTION).insertMany(documents);
        assertExportSnapshot(documents, "fresh full-export seed");
    }

    private ObjectNode exportTimetableRequest(Document document) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("region", ShuttleBusRegion.valueOf(document.getString("region")).getLabel());
        request.put("route_type", ShuttleRouteType.valueOf(document.getString("route_type")).getLabel());
        request.put("route_name", document.getString("route_name"));
        request.put("sub_name", document.getString("sub_name"));
        request.set("node_info", objectMapper.valueToTree(document.get("node_info")));
        request.set("route_info", objectMapper.valueToTree(document.get("route_info")));
        return request;
    }

    private void assertExportSnapshot(List<Document> expected, String phase) {
        List<Document> actual = mongoTemplate.getCollection(COLLECTION).find().into(new ArrayList<>());
        assertThat(actual).as("%s document count", phase).hasSize(expected.size());
        assertThat(actual).extracting(document -> document.getObjectId("_id"))
            .as("%s ObjectId set", phase)
            .containsExactlyInAnyOrderElementsOf(expected.stream()
                .map(document -> document.getObjectId("_id")).toList());
        Map<ObjectId, Document> actualById = actual.stream()
            .collect(Collectors.toMap(document -> document.getObjectId("_id"), document -> document));
        for (Document document : expected) {
            ObjectId id = document.getObjectId("_id");
            assertThat(normalizeMissingAndNullFields(actualById.get(id)))
                .as("%s _id=%s semester=%s route=%s", phase, id,
                    document.getString("semester_type"), document.getString("route_name"))
                .usingRecursiveComparison()
                .isEqualTo(normalizeMissingAndNullFields(document));
        }
    }

    private Object normalizeMissingAndNullFields(Object value) {
        if (value instanceof Document document) {
            Document normalized = new Document();
            document.forEach((key, field) -> {
                if (field != null) {
                    normalized.put(key, normalizeMissingAndNullFields(field));
                }
            });
            return normalized;
        }
        if (value instanceof List<?> list) {
            // 배열 순서와 null, 빈 문자열, 미정차, 하차 등 실제 값은 변환하지 않는다.
            return list.stream().map(this::normalizeMissingAndNullFields).toList();
        }
        return value;
    }

    private ResponseEntity<?> putTimetable(ObjectNode request) {
        return putTimetable(request, "PARTIAL");
    }

    private ResponseEntity<?> putTimetable(ObjectNode request, String updateMode) {
        return putJson(
            "/admin/bus/shuttle/timetable?semester_type=" + SemesterType.REGULAR.name()
                + "&update_mode=" + updateMode,
            wrapRequest(request)
        );
    }

    private ResponseEntity<String> putJson(String path, ObjectNode request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
            path,
            org.springframework.http.HttpMethod.PUT,
            new HttpEntity<>(request.toString(), headers),
            String.class
        );
    }

    private ObjectNode wrapRequest(ObjectNode timetable) {
        ObjectNode request = objectMapper.createObjectNode();
        ArrayNode timetables = objectMapper.createArrayNode();
        timetables.add(timetable);
        request.set("shuttle_bus_timetables", timetables);
        return request;
    }

    private ObjectNode baseRequest() throws IOException {
        Document fixture = loadFixture();
        ObjectNode request = objectMapper.createObjectNode();
        request.put("region", "천안・아산");
        request.put("route_type", "순환");
        request.put("route_name", "천안 셔틀");
        request.put("sub_name", "주중");
        request.set("node_info", objectMapper.valueToTree(fixture.get("node_info")));
        return request;
    }

    private ObjectNode fullRequestFromMongo() {
        Document fixture = mongoTemplate.getCollection(COLLECTION)
            .find(Filters.eq("_id", TIMETABLE_ID))
            .first();
        ObjectNode request = objectMapper.createObjectNode();
        request.put("region", "천안・아산");
        request.put("route_type", "순환");
        request.put("route_name", "천안 셔틀");
        request.put("sub_name", "주중");
        request.set("node_info", objectMapper.valueToTree(fixture.get("node_info")));
        request.set("route_info", objectMapper.valueToTree(fixture.get("route_info")));
        return request;
    }

    private ObjectNode routeInfoRequest(String name, List<String> runningDays, List<String> arrivalTime) {
        ObjectNode routeInfo = objectMapper.createObjectNode();
        routeInfo.put("name", name);
        routeInfo.putNull("detail");
        if (runningDays == null) {
            routeInfo.putNull("running_days");
        } else {
            routeInfo.set("running_days", objectMapper.valueToTree(runningDays));
        }
        routeInfo.set("arrival_time", objectMapper.valueToTree(arrivalTime));
        return routeInfo;
    }

    @SuppressWarnings("unchecked")
    private Document routeInfo(int index) {
        Document timetable = mongoTemplate.getCollection(COLLECTION)
            .find(Filters.eq("_id", TIMETABLE_ID))
            .first();
        return ((List<Document>)timetable.get("route_info")).get(index);
    }

    @SuppressWarnings("unchecked")
    private void assertFullTimetableSnapshot() throws IOException {
        Document expected = loadFixture();
        List<Document> expectedRoutes = (List<Document>)expected.get("route_info");
        expectedRoutes.get(11).put("arrival_time", CORRECT_SATURDAY_SECOND);
        expectedRoutes.get(13).put("arrival_time", CORRECT_SUNDAY_SECOND);

        Document actual = mongoTemplate.getCollection(COLLECTION)
            .find(Filters.eq("_id", TIMETABLE_ID))
            .first();

        assertThat(mongoTemplate.getCollection(COLLECTION).countDocuments()).isEqualTo(1);
        assertThat(actual.getObjectId("_id")).isEqualTo(TIMETABLE_ID);
        assertThat(actual.getString("semester_type")).isEqualTo("정규학기");
        assertThat(actual.getString("route_name")).isEqualTo("천안 셔틀");
        assertThat(actual.getString("sub_name")).isEqualTo("주중");
        assertThat(actual.getList("node_info", Document.class))
            .isEqualTo(expected.getList("node_info", Document.class));

        List<Document> actualRoutes = (List<Document>)actual.get("route_info");
        assertThat(actualRoutes).hasSize(16);
        for (int i = 0; i < expectedRoutes.size(); i++) {
            assertThat(actualRoutes.get(i).getString("name")).isEqualTo(expectedRoutes.get(i).getString("name"));
            assertThat(actualRoutes.get(i).getList("running_days", String.class))
                .isEqualTo(expectedRoutes.get(i).getList("running_days", String.class));
            assertThat(actualRoutes.get(i).getList("arrival_time", String.class))
                .isEqualTo(expectedRoutes.get(i).getList("arrival_time", String.class));
        }
    }

    private Document loadFixture() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FIXTURE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Fixture not found: " + FIXTURE);
            }
            Document document = Document.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            document.put("_id", TIMETABLE_ID);
            return document;
        }
    }

    private static class TestAuthArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return parameter.hasParameterAnnotation(Auth.class);
        }

        @Override
        public Object resolveArgument(
            org.springframework.core.MethodParameter parameter,
            org.springframework.web.method.support.ModelAndViewContainer mavContainer,
            org.springframework.web.context.request.NativeWebRequest webRequest,
            org.springframework.web.bind.support.WebDataBinderFactory binderFactory
        ) {
            return 1;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
    })
    @EnableMongoRepositories(basePackageClasses = {
        AdminShuttleBusTimetableRepository.class,
        AdminCommutingBusRepository.class
    })
    @Import({
        AdminCommutingBusController.class,
        AdminCommutingBusService.class,
        AdminCommutingBusExcelService.class,
        AdminCommutingBusDateExtractor.class,
        AdminCommutingBusExcelMetaDataExtractor.class,
        AdminCommutingBusNodeInfoRowIndexExtractor.class,
        AdminCommutingBusRouteInfoExtractor.class,
        AdminShuttleBusTimetableController.class,
        AdminShuttleBusExcelService.class,
        AdminShuttleBusService.class,
        GlobalExceptionHandler.class,
        TestWebMvcConfig.class
    })
    static class TestApplication {
    }

    @TestConfiguration
    static class TestWebMvcConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new TestAuthArgumentResolver());
        }
    }
}
