package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.AbtestFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
public class AbtestApiTest extends AcceptanceTest {

    @Autowired
    private AbtestFixture abtestFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private AbtestRepository abtestRepository;

    @Test
    @DisplayName("실험을 생성한다.")
    void createAbtest() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
            .body(String.format("""
                {
                  "display_title": "사장님 전화번호 회원가입 실험",
                  "creater": "송선권",
                  "team": "campus",
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
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                  "id": 1,
                  "display_title": "사장님 전화번호 회원가입 실험",
                  "creater": "송선권",
                  "team": "campus",
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
                """);
    }

    @Test
    @DisplayName("실험을 단건 조회한다.")
    void getAbtest() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);
        Abtest abtest = abtestFixture.식단_UI_실험();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
            .when()
            .get("/abtest/{id}", abtest.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""

                """, abtest.getId()));
    }

    @Test
    @DisplayName("실험 목록을 조회한다.")
    void getAbtests() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);
        Abtest abtest1 = abtestFixture.식단_UI_실험();
        Abtest abtest2 = abtestFixture.주변상점_UI_실험();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
            .when()
            .get("/abtest")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                """));
    }

    @Test
    @DisplayName("실험 목록을 조회한다. - 페이지네이션")
    void getAbtestsWithPaging() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);
        for (int i = 0; i < 10; i++) {
            abtestFixture.식단_UI_실험();
        }

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
            .when()
            .get("/abtest")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                """));
    }

    @Test
    @DisplayName("실험을 수정한다.")
    void putAbtest() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);
        Abtest abtest = abtestFixture.식단_UI_실험();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
            .body(String.format("""
                                
                """))
            .when()
            .put("/abtest/{id}", abtest.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                """, abtest.getId()));
    }

    @Test
    @DisplayName("실험을 삭제한다.")
    void deleteAbtest() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);
        Abtest abtest = abtestFixture.식단_UI_실험();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
            .when()
            .delete("/abtest/{id}", abtest.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        assertThat(abtestRepository.findById(abtest.getId())).isNotPresent();
    }

    @Test
    @DisplayName("실험을 종료한다.")
    void closeAbtest() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);
        Abtest abtest = abtestFixture.식단_UI_실험();

        String winner = "A";

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
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

        assertThat(abtest.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("(실험군 수동편입) 이름으로 유저 목록을 조회한다.")
    void getUsersByUserName() {
        User adminUser = userFixture.코인_운영자();
        Student student = userFixture.성빈_학생();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
            .queryParam("name", student.getUser().getName())
            .when()
            .get("/abtest/user")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                """));
    }

    @Test
    @DisplayName("(실험군 수동편입) 유저 ID로 기기 목록을 조회한다.")
    void getDevicesByUserId() {
        User adminUser = userFixture.코인_운영자();
        Student student = userFixture.성빈_학생();
        Device device = abtestFixture.아이폰(student.getUser().getId());
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
            .pathParam("userId", student.getUser().getId())
            .when()
            .get("/abtest/user/{userId}/device")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                """));
    }

    @Test
    @DisplayName("특정 유저의 실험군을 수동으로 편입시킨다.")
    void moveAbtestVariable() {
        User adminUser = userFixture.코인_운영자();
        Student student = userFixture.성빈_학생();
        Device device = abtestFixture.아이폰(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer" + token)
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

        // 특정 유저의 실험군이 바뀌었는지 확인하는 코드 필요
    }

    @Test
    @DisplayName("자신의 실험군을 조회한다.")
    void getMyAbtestVariable() {
        Student student = userFixture.성빈_학생();
        Device device = abtestFixture.아이폰(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();
        String token = userFixture.getToken(student.getUser());

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("X-Forwarded-For", device.getAccessHistory().getPublicIp())
            .queryParam("title", abtest.getName())
            .when()
            .get("/abtest/me")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        // 실험이름으로 조건걸어서 확인하기. 엔티티 수정 후 마저 작성
/*
        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(device.getAccessHistory().getVariable().getName());
*/
    }

    @Test
    @DisplayName("(실험군 자동 편입)실험군에 최초로 편입된다.")
    void assignAbtest() {
        Student student = userFixture.성빈_학생();
        Device device = abtestFixture.아이폰(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("X-Forwarded-For", device.getAccessHistory().getPublicIp())
            .body(String.format("""
                {
                  "title": "business.register.phone_number"
                }
                """))
            .when()
            .get("/abtest/assign")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        // 위 테스트와 동일
    }
}
