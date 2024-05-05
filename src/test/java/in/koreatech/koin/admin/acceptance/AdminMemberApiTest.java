package in.koreatech.koin.admin.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.TrackRepository;
import in.koreatech.koin.fixture.MemberFixture;
import in.koreatech.koin.fixture.TrackFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
public class AdminMemberApiTest extends AcceptanceTest {

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private TrackFixture trackFixture;

    @Test
    @DisplayName("BCSDLab 회원들의 정보를 조회한다")
    void getMembers() {
        memberFixture.최준호(trackFixture.backend());

        var response = RestAssured
            .given()
            .when()
            .param("page", 1)
            .param("track", "BACKEND")
            .param("is_deleted", false)
            .get("/admin/members")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "total_count": 1,
                    "current_count": 1,
                    "total_page": 1,
                    "current_page": 1,
                    "members": [
                        {
                            "id": 1,
                            "name": "최준호",
                            "student_number": "2019136135",
                            "track": "BackEnd",
                            "position": "Regular",
                            "email": "testjuno@gmail.com",
                            "image_url": "https://imagetest.com/juno.jpg",
                            "is_deleted": false
                        }
                    ]
                }
                """
            );
    }
}
