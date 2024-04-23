package in.koreatech.koin.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.fixture.MemberFixture;
import in.koreatech.koin.fixture.TechStackFixture;
import in.koreatech.koin.fixture.TrackFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
class TrackApiTest extends AcceptanceTest {

    @Autowired
    private TrackFixture trackFixture;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private TechStackFixture techStackFixture;

    @Test
    @DisplayName("BCSDLab 트랙 정보를 조회한다")
    void findTracks() {
        trackFixture.backend();
        trackFixture.frontend();
        trackFixture.ios();

        var response = RestAssured
            .given()
            .when()
            .get("/tracks")
            .then()
            .log().all()

            .log().all()
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
    @DisplayName("BCSDLab 트랙 정보 단건 조회")
    void findTrack() {
        Track track = trackFixture.backend();
        memberFixture.최준호(track);
        techStackFixture.java(track);

        var response = RestAssured
            .given()
            .when()
            .get("/tracks/{id}", track.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                     "TrackName": "BackEnd",
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
                         }
                     ],
                     "Members": [
                         {
                             "id": 1,
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
                     ]
                 }
                """);
    }

    @Test
    @DisplayName("BCSDLab 트랙 정보 단건 조회 - 트랙에 속한 멤버와 기술스택이 없을 때")
    void findTrackWithEmptyMembersAndTechStacks() {
        Track track = trackFixture.frontend();

        var response = RestAssured
            .given()
            .when()
            .get("/tracks/{id}", track.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "TrackName": "FrontEnd",
                    "TechStacks": [
                      
                    ],
                    "Members": [
                       
                    ]
                }
                """);
    }
}
