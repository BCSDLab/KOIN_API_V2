package in.koreatech.koin.admin.acceptance;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.member.repository.AdminMemberRepository;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.TrackRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.MemberFixture;
import in.koreatech.koin.fixture.TrackFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
public class AdminMemberApiTest extends AcceptanceTest {

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private TrackFixture trackFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private AdminMemberRepository adminMemberRepository;

    @Test
    @DisplayName("BCSDLab 회원들의 정보를 조회한다")
    void getMembers() {
        memberFixture.최준호(trackFixture.backend());

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
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

    @Test
    @DisplayName("관리자 권한으로 BCSDLab 회원을 추가한다.")
    void postMember() {
        trackFixture.backend();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        String jsonBody = """
            {
                "name": "최준호",
                "student_number": "2019136135",
                "track": "BackEnd",
                "position": "Regular",
                "email": "testjuno@gmail.com",
                "image_url": "https://imagetest.com/juno.jpg"
            }
            """;

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body(jsonBody)
            .when()
            .post("/admin/members")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract().asString();

        Member savedMember = adminMemberRepository.getByName("최준호");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(savedMember.getName()).isEqualTo("최준호");
            softly.assertThat(savedMember.getStudentNumber()).isEqualTo("2019136135");
            softly.assertThat(savedMember.getTrack().getName()).isEqualTo("BackEnd");
            softly.assertThat(savedMember.getPosition()).isEqualTo("Regular");
            softly.assertThat(savedMember.getEmail()).isEqualTo("testjuno@gmail.com");
            softly.assertThat(savedMember.getImageUrl()).isEqualTo("https://imagetest.com/juno.jpg");
            softly.assertThat(savedMember.isDeleted()).isEqualTo(false);
        });
    }

    @Test
    @DisplayName("BCSDLab 회원 정보를 조회한다")
    void getMember() {
        memberFixture.최준호(trackFixture.backend());

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/admin/members/{id}", 1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """
            );
    }

    @Test
    @DisplayName("BCSDLab 회원 정보를 삭제한다")
    void deleteMember() {
        Member member = memberFixture.최준호(trackFixture.backend());
        Integer memberId = member.getId();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/admin/members/{id}", memberId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();


        Member savedMember = adminMemberRepository.getById(memberId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(savedMember.getName()).isEqualTo("최준호");
            softly.assertThat(savedMember.getStudentNumber()).isEqualTo("2019136135");
            softly.assertThat(savedMember.getTrack().getName()).isEqualTo("BackEnd");
            softly.assertThat(savedMember.getPosition()).isEqualTo("Regular");
            softly.assertThat(savedMember.getEmail()).isEqualTo("testjuno@gmail.com");
            softly.assertThat(savedMember.getImageUrl()).isEqualTo("https://imagetest.com/juno.jpg");
            softly.assertThat(savedMember.isDeleted()).isEqualTo(true);
        });
    }
}
