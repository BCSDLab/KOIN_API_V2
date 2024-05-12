package in.koreatech.koin.admin.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.TrackFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
public class AdminTrackApiTest extends AcceptanceTest {

    @Autowired
    private TrackFixture trackFixture;

    @Autowired
    private UserFixture userFixture;

    @Test
    @DisplayName("관리자가 BCSDLab 트랙 정보를 조회한다 - 관리자가 아니면 403 반환")
    void findTracksAdminNoAuth() {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/admin/tracks")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("관리자가 BCSDLab 트랙 정보를 조회한다")
    void findTracks() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        trackFixture.backend();
        trackFixture.frontend();
        trackFixture.ios();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/admin/tracks")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "id": 1,
                        "name": "BackEnd",
                        "headcount": 0,
                        "is_deleted": false,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                        "id": 2,
                        "name": "FrontEnd",
                        "headcount": 0,
                        "is_deleted": false,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                        "id": 3,
                        "name": "iOS",
                        "headcount": 0,
                        "is_deleted": false,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    }
                ]
                """);
    }
}
