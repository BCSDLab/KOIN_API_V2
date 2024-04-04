package in.koreatech.koin.acceptance;

import static java.time.format.DateTimeFormatter.ofPattern;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.MemberRepository;
import in.koreatech.koin.domain.member.repository.TrackRepository;
import io.restassured.RestAssured;

class MemberApiTest extends AcceptanceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Test
    @DisplayName("BCSDLab 회원의 정보를 조회한다")
    void getMember() {
        Track track = Track.builder().name("BackEnd").build();
        trackRepository.save(track);

        Member member = Member.builder()
            .isDeleted(false)
            .studentNumber("2019136135")
            .imageUrl("https://imagetest.com/juno.jpg")
            .name("최준호")
            .position("Regular")
            .track(track)
            .email("juno@gmail.com")
            .build();

        memberRepository.save(member);

        var response = RestAssured
            .given()
            .when()
            .get("/members/{id}", member.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Assertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                    {
                        "id": %d,
                        "name": "최준호",
                        "student_number": "2019136135",
                        "track": "BackEnd",
                        "position": "Regular",
                        "email": "juno@gmail.com",
                        "image_url": "https://imagetest.com/juno.jpg",
                        "is_deleted": false,
                        "created_at": "%s",
                        "updated_at": "%s"
                    }""",
                member.getId(),
                member.getCreatedAt().format(ofPattern("yyyy-MM-dd HH:mm:ss")),
                member.getUpdatedAt().format(ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
    }

    @Test
    @DisplayName("BCSDLab 회원들의 정보를 조회한다")
    void getMembers() {
        Track track = Track.builder().name("BackEnd").build();
        trackRepository.save(track);

        Member member = Member.builder()
            .isDeleted(false)
            .studentNumber("2019136064")
            .imageUrl("https://imagetest.com/asdf.jpg")
            .name("박한수")
            .position("Regular")
            .track(track)
            .email("hsp@gmail.com")
            .build();

        Member member2 = Member.builder()
            .isDeleted(false)
            .studentNumber("2019136135")
            .imageUrl("https://imagetest.com/juno.jpg")
            .name("최준호")
            .position("Regular")
            .track(track)
            .email("juno@gmail.com")
            .build();

        memberRepository.save(member);
        memberRepository.save(member2);

        var response = RestAssured
            .given()
            .when()
            .get("/members")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Assertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                    [
                        {
                            "id": 1,
                            "name": "박한수",
                            "student_number": "2019136064",
                            "track": "BackEnd",
                            "position": "Regular",
                            "email": "hsp@gmail.com",
                            "image_url": "https://imagetest.com/asdf.jpg",
                            "is_deleted": false,
                            "created_at": "%s",
                            "updated_at": "%s"
                        },
                        {
                            "id": 2,
                            "name": "최준호",
                            "student_number": "2019136135",
                            "track": "BackEnd",
                            "position": "Regular",
                            "email": "juno@gmail.com",
                            "image_url": "https://imagetest.com/juno.jpg",
                            "is_deleted": false,
                            "created_at": "%s",
                            "updated_at": "%s"
                        }
                    ]""",
                member.getCreatedAt().format(ofPattern("yyyy-MM-dd HH:mm:ss")),
                member.getUpdatedAt().format(ofPattern("yyyy-MM-dd HH:mm:ss")),
                member2.getUpdatedAt().format(ofPattern("yyyy-MM-dd HH:mm:ss")),
                member2.getUpdatedAt().format(ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
    }
}
