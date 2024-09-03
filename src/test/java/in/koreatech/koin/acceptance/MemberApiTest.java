package in.koreatech.koin.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.TrackRepository;
import in.koreatech.koin.fixture.MemberFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
class MemberApiTest extends AcceptanceTest {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private MemberFixture memberFixture;

    Track backend;
    Track frontend;

    @BeforeEach
    void setUp() {
        backend = trackRepository.save(
            Track.builder()
                .name("BackEnd")
                .build()
        );
        frontend = trackRepository.save(
            Track.builder()
                .name("FrontEnd")
                .build()
        );
    }

    @Test
    @DisplayName("BCSDLab 회원의 정보를 조회한다")
    void getMember() {
        Member member = memberFixture.최준호(backend);

        var response = RestAssured
            .given()
            .when()
            .get("/members/{id}", member.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                    {
                        "id": %d,
                        "name": "최준호",
                        "student_number": "2019136135",
                        "track": "BackEnd",
                        "position": "Regular",
                        "email": "testjuno@gmail.com",
                        "image_url": "https://imagetest.com/juno.jpg",
                        "is_deleted": false,
                        "created_at": "%s",
                        "updated_at": "%s"
                    }""",
                member.getId(),
                response.jsonPath().getString("created_at"),
                response.jsonPath().getString("updated_at")
            ));
    }

    @Test
    @DisplayName("BCSDLab 회원들의 정보를 조회한다")
    void getMembers() {
        memberFixture.최준호(backend);
        memberFixture.박한수(frontend);

        var response = RestAssured
            .given()
            .when()
            .get("/members")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "id": 1,
                        "name": "최준호",
                        "student_number": "2019136135",
                        "track": "BackEnd",
                        "position": "Regular",
                        "email": "testjuno@gmail.com",
                        "image_url": "https://imagetest.com/juno.jpg",
                        "is_deleted": false,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                        "id": 2,
                        "name": "박한수",
                        "student_number": "2019136064",
                        "track": "FrontEnd",
                        "position": "Regular",
                        "email": "testhsp@gmail.com",
                        "image_url": "https://imagetest.com/juno.jpg",
                        "is_deleted": false,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    }
                ]
                """
            );
    }
}
