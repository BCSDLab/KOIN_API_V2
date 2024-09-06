package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.model.AccessHistoryAbtestVariable;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.AbtestFixture;
import in.koreatech.koin.fixture.DeviceFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class AbtestApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AbtestFixture abtestFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private DeviceFixture deviceFixture;

    @Autowired
    private AbtestRepository abtestRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    private User admin;
    private String adminToken;

    @BeforeEach
    void setUp() {
        admin = userFixture.코인_운영자();
        adminToken = userFixture.getToken(admin);
    }

    @Test
    @DisplayName("실험을 생성한다.")
    void createAbtest() {

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .body(String.format("""
                {
                  "display_title": "사장님 전화번호 회원가입 실험",
                  "creator": "송선권",
                  "team": "campus",
                  "description": "세부설명",
                  "status": "IN_PROGRESS",
                  "title": "business.register.phone_number",
                  "variables": [
                    {
                      "rate": 33,
                      "display_name": "실험군 A",
                      "name": "A"
                    },
                    {
                      "rate": 33,
                      "display_name": "실험군 B",
                      "name": "B"
                    },
                    {
                      "rate": 34,
                      "display_name": "실험군 C",
                      "name": "C"
                    }
                  ]
                } 
                """))
            .when()
            .post("/abtest")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                  "id": 1,
                  "display_title": "사장님 전화번호 회원가입 실험",
                  "creator": "송선권",
                  "team": "campus",
                  "description": "세부설명",
                  "title": "business.register.phone_number",
                  "status": "IN_PROGRESS",
                  "winner_name": null,
                  "variables": [
                    {
                      "rate": 33,
                      "display_name": "실험군 A",
                      "name": "A"
                    },
                    {
                      "rate": 33,
                      "display_name": "실험군 B",
                      "name": "B"
                    },
                    {
                      "rate": 34,
                      "display_name": "실험군 C",
                      "name": "C"
                    }
                  ],
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }

    @Test
    @DisplayName("실험을 단건 조회한다.")
    void getAbtest() {
        Abtest abtest = abtestFixture.식단_UI_실험();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .get("/abtest/{id}", abtest.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                  "id": 1,
                  "display_title": "식단_UI_실험",
                  "creator": "송선권",
                  "team": "campus",
                  "description": "세부설명",
                  "title": "dining_ui_test",
                  "status": "IN_PROGRESS",
                  "winner_name": null,
                  "variables": [
                    {
                      "rate": 50,
                      "display_name": "실험군 A",
                      "name": "A"
                    },
                    {
                      "rate": 50,
                      "display_name": "실험군 B",
                      "name": "B"
                    }
                  ],
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15 12:00:00"
                }
                """, abtest.getId()));
    }

    @Test
    @DisplayName("실험 목록을 조회한다.")
    void getAbtests() {
        Abtest abtest1 = abtestFixture.식단_UI_실험();
        Abtest abtest2 = abtestFixture.주변상점_UI_실험();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .get("/abtest")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                  "total_count": 2,
                  "current_count": 2,
                  "total_page": 1,
                  "current_page": 1,
                  "tests": [
                    {
                      "id": 2,
                      "status": "IN_PROGRESS",
                      "creator": "송선권",
                      "team": "campus",
                      "display_title": "주변상점_UI_실험",
                      "title": "shop_ui_test",
                      "winner_name": null,
                      "created_at": "2024-01-15 12:00:00",
                      "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                      "id": 1,
                      "status": "IN_PROGRESS",
                      "creator": "송선권",
                      "team": "campus",
                      "display_title": "식단_UI_실험",
                      "title": "dining_ui_test",
                      "winner_name": null,
                      "created_at": "2024-01-15 12:00:00",
                      "updated_at": "2024-01-15 12:00:00"
                    }
                  ]
                }
                """));
    }

    @Test
    @DisplayName("실험 목록을 조회한다. - 페이지네이션")
    void getAbtestsWithPaging() {
        for (int i = 0; i < 10; i++) {
            abtestFixture.식단_UI_실험();
        }

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .queryParam("page", 2)
            .queryParam("limit", 8)
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .get("/abtest")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                  "total_count": 10,
                  "current_count": 2,
                  "total_page": 2,
                  "current_page": 2,
                  "tests": [
                    {
                      "id": 2,
                      "status": "IN_PROGRESS",
                      "creator": "송선권",
                      "team": "campus",
                      "display_title": "식단_UI_실험",
                      "title": "dining_ui_test",
                      "winner_name": null,
                      "created_at": "2024-01-15 12:00:00",
                      "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                      "id": 1,
                      "status": "IN_PROGRESS",
                      "creator": "송선권",
                      "team": "campus",
                      "display_title": "식단_UI_실험",
                      "title": "dining_ui_test",
                      "winner_name": null,
                      "created_at": "2024-01-15 12:00:00",
                      "updated_at": "2024-01-15 12:00:00"
                    }
                  ]
                }
                """));
    }

    @Test
    @DisplayName("실험을 수정한다.")
    void putAbtest() {
        Abtest abtest = abtestFixture.식단_UI_실험();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .body(String.format("""
                {
                  "display_title": "식단_UI_실험",
                  "creator": "김성재",
                  "team": "user",
                  "title": "dining_ui_test",
                  "description": "세부설명2",
                  "variables": [
                    {
                      "rate": 10,
                      "display_name": "실험군 A",
                      "name": "A"
                    },
                    {
                      "rate": 90,
                      "display_name": "실험군 B",
                      "name": "B"
                    }
                  ],
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15 12:00:00"
                }
                """))
            .when()
            .put("/abtest/{id}", abtest.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                  "id": 1,
                 "display_title": "식단_UI_실험",
                  "creator": "김성재",
                  "team": "user",
                  "title": "dining_ui_test",
                  "status": "IN_PROGRESS",
                  "description": "세부설명2",
                  "winner_name": null,
                  "variables": [
                    {
                      "rate": 10,
                      "display_name": "실험군 A",
                      "name": "A"
                    },
                    {
                      "rate": 90,
                      "display_name": "실험군 B",
                      "name": "B"
                    }
                  ],
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }

    @Test
    @DisplayName("실험을 삭제한다.")
    void deleteAbtest() {
        Abtest abtest = abtestFixture.식단_UI_실험();

        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .log().all()
            .when()
            .delete("/abtest/{id}", abtest.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        assertThat(abtestRepository.findById(abtest.getId())).isNotPresent();
    }

    @Test
    @DisplayName("실험을 종료한다.")
    void closeAbtest() {
        final Abtest abtest = abtestFixture.식단_UI_실험();
        String winner = "A";

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .body(String.format("""
                {
                  "winner_name": "%s"
                }
                """, winner))
            .when()
            .post("/abtest/close/{id}", abtest.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            Abtest result = abtestRepository.getById(abtest.getId());
            assertSoftly(
                softly -> {
                    softly.assertThat(result.getStatus().name()).isEqualTo("CLOSED");
                    softly.assertThat(result.getWinner().getName()).isEqualTo(winner);
                }
            );
        });
    }

    @Test
    @DisplayName("(실험군 수동편입) 이름으로 유저 목록을 조회한다.")
    void getUsersByUserName() {
        Student student = userFixture.성빈_학생();
        Owner owner = userFixture.성빈_사장님();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("name", student.getUser().getName())
            .when()
            .get("/abtest/user")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                  "users": [
                    {
                    "id": %d,
                    "name" : "박성빈",
                    "detail": "testsungbeen@koreatech.ac.kr"
                    },
                    {
                    "id": %d,
                    "name" : "박성빈",
                    "detail": "01098765439"
                    }
                  ]
                }
                """, student.getUser().getId(), owner.getUser().getId()));
    }

    @Test
    @DisplayName("(실험군 수동편입) 유저 ID로 기기 목록을 조회한다.")
    void getDevicesByUserId() {
        Student student = userFixture.성빈_학생();
        Device device1 = deviceFixture.아이폰(student.getUser().getId());
        Device device2 = deviceFixture.갤럭시(student.getUser().getId());

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .pathParam("userId", student.getUser().getId())
            .when()
            .get("/abtest/user/{userId}/device")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                  "devices": [
                    {
                    "id": %d,
                    "type": "mobile",
                    "model" : "아이폰14",
                    "last_accessed_at": "2024-01-15"
                    },
                    {
                    "id": %d,
                    "type": "mobile",
                    "model" : "갤럭시24",
                    "last_accessed_at": "2024-01-15"
                    }
                  ]
                }
                """, device1.getId(), device2.getId()));
    }

    @Test
    @DisplayName("특정 유저의 실험군을 수동으로 편입시킨다.")
    void moveAbtestVariable() {
        Student student = userFixture.성빈_학생();
        Device device = deviceFixture.아이폰(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .body(String.format("""
                {
                  "device_id": %d,
                  "variable_name": "A"
                }
                """, device.getId()))
            .when()
            .pathParam("id", abtest.getId())
            .post("/abtest/{id}/move")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();


        transactionTemplate.executeWithoutResult(status -> {
            assertSoftly(
                softly -> {
                    Device result = deviceRepository.getById(device.getId());
                    Optional<AbtestVariable> variable = result.getAccessHistory()
                        .getAccessHistoryAbtestVariables()
                        .stream()
                        .map(AccessHistoryAbtestVariable::getVariable)
                        .filter(var -> var.getAbtest().getTitle().equals(abtest.getTitle()))
                        .findAny();
                    softly.assertThat(variable.get().getName()).isEqualTo("A");
                }
            );
        });
    }

    @Test
    @DisplayName("자신의 실험군을 조회한다.")
    void getMyAbtestVariable() {
        Student student = userFixture.성빈_학생();
        final Device device = deviceFixture.아이폰(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("access_history_id", device.getAccessHistory().getId())
            .body(String.format("""
                {
                  "title": "dining_ui_test"
                }
                """))
            .when()
            .post("/abtest/assign")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("access_history_id", device.getAccessHistory().getId())
            .queryParam("title", abtest.getTitle())
            .when()
            .get("/abtest/me")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            assertSoftly(
                softly -> {
                    Device result = deviceRepository.getById(device.getId());
                    Optional<AbtestVariable> variable = result.getAccessHistory()
                        .getAccessHistoryAbtestVariables()
                        .stream()
                        .map(AccessHistoryAbtestVariable::getVariable)
                        .filter(var -> var.getAbtest().getTitle().equals(abtest.getTitle()))
                        .findAny();
                    softly.assertThat(response.asPrettyString()).isEqualTo(variable.get().getName());
                }
            );
        });
    }

    @Test
    @DisplayName("(실험군 자동 편입)실험군에 최초로 편입된다.")
    void assignAbtest() {
        Student student = userFixture.성빈_학생();
        Device device1 = deviceFixture.아이폰(student.getUser().getId());
        Device device2 = deviceFixture.갤럭시(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();

        var response1 = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("access_history_id", device1.getAccessHistory().getId())
            .body(String.format("""
                {
                  "title": "dining_ui_test"
                }
                """))
            .when()
            .post("/abtest/assign")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        var response2 = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("access_history_id", device2.getAccessHistory().getId())
            .body(String.format("""
                {
                  "title": "dining_ui_test"
                }
                """))
            .when()
            .post("/abtest/assign")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        String res1 = response1.asPrettyString();
        String res2 = response2.asPrettyString();
        assertNotEquals(res1, res2);
    }
}
