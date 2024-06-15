package in.koreatech.koin.admin.acceptance;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.member.repository.AdminTechStackRepository;
import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.MemberFixture;
import in.koreatech.koin.fixture.TechStackFixture;
import in.koreatech.koin.fixture.TrackFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
public class AdminTrackApiTest extends AcceptanceTest {

    @Autowired
    private TrackFixture trackFixture;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private TechStackFixture techStackFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private AdminTechStackRepository adminTechStackRepository;

    @Test
    @DisplayName("관리자가 BCSDLab 트랙 정보를 조회한다 - 관리자가 아니면 403 반환")
    void findTracksAdminNoAuth() {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        RestAssured
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

    @Test
    @DisplayName("관리자가 BCSDLab 트랙 정보를 생성한다.")
    void createTrack() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
                {
                  "name": "BackEnd",
                  "headcount": 20
                }
                """)
            .when()
            .post("/admin/tracks")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "id": 1,
                    "name": "BackEnd",
                    "headcount": 20,
                    "is_deleted": false,
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }

    @Test
    @DisplayName("관리자가 BCSDLab 트랙 정보를 생성한다. - 이미 있는 트랙명이면 409반환")
    void createTrackDuplication() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        trackFixture.backend();

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
                {
                  "name": "BackEnd",
                  "headcount": 20
                }
                """)
            .when()
            .post("/admin/tracks")
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .extract();
    }

    @Test
    @DisplayName("관리자가 BCSDLab 트랙 단건 정보를 조회한다.")
    void findTrack() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        Track backend = trackFixture.backend();
        trackFixture.ai();                    // 삭제된 트랙
        memberFixture.배진호(backend);         // 삭제된 멤버
        memberFixture.최준호(backend);
        techStackFixture.java(backend);
        techStackFixture.adobeFlash(backend); //삭제된 기술스택

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/admin/tracks/{id}", backend.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                     "TrackName": "BackEnd",
                     "Members": [
                         {
                             "id": 1,
                             "name": "배진호",
                             "student_number": "2020136061",
                             "position": "Regular",
                             "track": "BackEnd",
                             "email": "testjhb@gmail.com",
                             "image_url": "https://imagetest.com/jino.jpg",
                             "is_deleted": true,
                             "created_at": "2024-01-15 12:00:00",
                             "updated_at": "2024-01-15 12:00:00"
                         },
                         {
                             "id": 2,
                             "name": "최준호",
                             "student_number": "2019136135",
                             "position": "Regular",
                             "track": "BackEnd",
                             "email": "testjuno@gmail.com",
                             "image_url": "https://imagetest.com/juno.jpg",
                             "is_deleted": false,
                             "created_at": "2024-01-15 12:00:00",
                             "updated_at": "2024-01-15 12:00:00"
                         }
                     ],
                     "TechStacks": [
                         {
                             "id": 1,
                             "name": "Java",
                             "description": "Language",
                             "image_url": "https://testimageurl.com",
                             "track_id": 1,
                             "is_deleted": false,
                             "created_at": "2024-01-15 12:00:00",
                             "updated_at": "2024-01-15 12:00:00"
                         },
                         {
                             "id": 2,
                             "name": "AdobeFlash",
                             "description": "deleted",
                             "image_url": "https://testimageurl.com",
                             "track_id": 1,
                             "is_deleted": true,
                             "created_at": "2024-01-15 12:00:00",
                             "updated_at": "2024-01-15 12:00:00"
                         }
                     ]
                 }
                """);
    }

    @Test
    @DisplayName("관리자가 BCSDLab 트랙 정보를 수정한다.")
    void updateTrack() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        Track backEnd = trackFixture.backend();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
                {
                  "name": "frontEnd",
                  "headcount": 20
                }
                """)
            .when()
            .put("/admin/tracks/{id}", backEnd.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "id": 1,
                    "name": "frontEnd",
                    "headcount": 20,
                    "is_deleted": false,
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }

    @Test
    @DisplayName("관리자가 BCSDLab 트랙 정보를 수정한다. - 이미 있는 트랙명이면 409반환")
    void updateTrackDuplication() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        Track backEnd = trackFixture.backend();

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
                {
                  "name": "BackEnd",
                  "headcount": 20
                }
                """)
            .when()
            .put("/admin/tracks/{id}", backEnd.getId())
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .extract();
    }

    @Test
    @DisplayName("관리자가 BCSDLab 기술스택 정보를 생성한다")
    void createTechStack() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        trackFixture.frontend();
        Track backEnd = trackFixture.backend();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
                {
                    "image_url": "https://url.com",
                    "name": "Spring",
                    "description": "스프링은 웹 프레임워크이다"
                }
                """)
            .when()
            .queryParam("trackName", backEnd.getName())
            .post("/admin/techStacks")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "id": 1,
                    "image_url": "https://url.com",
                    "name": "Spring",
                    "description": "스프링은 웹 프레임워크이다",
                    "track_id": 2,
                    "is_deleted": false,
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }

    @Test
    @DisplayName("관리자가 BCSDLab 기술스택 정보를 수정한다")
    void updateTechStack() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        TechStack java = techStackFixture.java(trackFixture.frontend());
        Track backEnd = trackFixture.backend();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
                {
                    "image_url": "https://java.com",
                    "name": "JAVA",
                    "description": "java의 TrackID를 BackEnd로 수정한다.",
                    "is_deleted": true
                }
                """)
            .when()
            .queryParam("trackName", backEnd.getName())
            .put("/admin/techStacks/{id}", java.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "id": 1,
                    "image_url": "https://java.com",
                    "name": "JAVA",
                    "description": "java의 TrackID를 BackEnd로 수정한다.",
                    "track_id": 2,
                    "is_deleted": true,
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }

    @Test
    @DisplayName("관리자가 기술스택 정보를 삭제한다")
    void deleteTechStack() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        Track backEnd = trackFixture.backend();
        TechStack java = techStackFixture.java(backEnd);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/admin/techStacks/{id}", java.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        TechStack techStack = adminTechStackRepository.getById(java.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(techStack.getImageUrl()).isEqualTo("https://testimageurl.com");
            softly.assertThat(techStack.getName()).isEqualTo("Java");
            softly.assertThat(techStack.getDescription()).isEqualTo("Language");
            softly.assertThat(techStack.getTrackId()).isEqualTo(backEnd.getId());
            softly.assertThat(techStack.isDeleted()).isEqualTo(true);
        });
    }
}
