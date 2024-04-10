package in.koreatech.koin.acceptance;

import java.time.format.DateTimeFormatter;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.MemberRepository;
import in.koreatech.koin.domain.member.repository.TechStackRepository;
import in.koreatech.koin.domain.member.repository.TrackRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class TrackApiTest extends AcceptanceTest {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private TechStackRepository techStackRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("BCSDLab 트랙 정보를 조회한다")
    void findTracks() {
        Track request = Track.builder().name("BackEnd").build();
        Track track = trackRepository.save(request);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/tracks")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getList(".").size()).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getInt("[0].id")).isEqualTo(track.getId());
                softly.assertThat(response.body().jsonPath().getString("[0].name")).isEqualTo(track.getName());
                softly.assertThat(response.body().jsonPath().getInt("[0].headcount")).isEqualTo(track.getHeadcount());
                softly.assertThat(response.body().jsonPath().getBoolean("[0].is_deleted"))
                    .isEqualTo(track.isDeleted());
                softly.assertThat(response.body().jsonPath().getString("[0].created_at"))
                    .contains(track.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                softly.assertThat(response.body().jsonPath().getString("[0].updated_at"))
                    .contains(track.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        );
    }

    @Test
    @DisplayName("BCSDLab 트랙 정보 단건 조회")
    void findTrack() {
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
        memberRepository.save(member);

        TechStack techStack = TechStack.builder()
            .isDeleted(false)
            .imageUrl("https://testimageurl.com")
            .trackId(track.getId())
            .name("Java")
            .description("Language")
            .build();
        techStackRepository.save(techStack);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/tracks/{id}", track.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("TrackName")).isEqualTo(track.getName());
                softly.assertThat(response.body().jsonPath().getList("Members")).hasSize(1);
                softly.assertThat(response.body().jsonPath().getInt("Members[0].id"))
                    .isEqualTo(member.getId().longValue());
                softly.assertThat(response.body().jsonPath().getString("Members[0].name")).isEqualTo(member.getName());
                softly.assertThat(response.body().jsonPath().getString("Members[0].student_number"))
                    .isEqualTo(member.getStudentNumber());
                softly.assertThat(response.body().jsonPath().getString("Members[0].position"))
                    .isEqualTo(member.getPosition());
                softly.assertThat(response.body().jsonPath().getString("Members[0].track"))
                    .isEqualTo(track.getName());
                softly.assertThat(response.body().jsonPath().getString("Members[0].email"))
                    .isEqualTo(member.getEmail());
                softly.assertThat(response.body().jsonPath().getString("Members[0].image_url"))
                    .isEqualTo(member.getImageUrl());
                softly.assertThat(response.body().jsonPath().getBoolean("Members[0].is_deleted"))
                    .isEqualTo(member.isDeleted());
                softly.assertThat(response.body().jsonPath().getString("Members[0].updated_at"))
                    .contains(member.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                softly.assertThat(response.body().jsonPath().getString("Members[0].created_at"))
                    .contains(member.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                softly.assertThat(response.body().jsonPath().getList("TechStacks")).hasSize(1);
                softly.assertThat(response.body().jsonPath().getInt("TechStacks[0].id")).isEqualTo(techStack.getId());
                softly.assertThat(response.body().jsonPath().getString("TechStacks[0].image_url"))
                    .isEqualTo(techStack.getImageUrl());
                softly.assertThat(response.body().jsonPath().getInt("TechStacks[0].track_id"))
                    .isEqualTo(techStack.getTrackId());
                softly.assertThat(response.body().jsonPath().getString("TechStacks[0].name"))
                    .isEqualTo(techStack.getName());
                softly.assertThat(response.body().jsonPath().getString("TechStacks[0].description"))
                    .isEqualTo(techStack.getDescription());
                softly.assertThat(response.body().jsonPath().getBoolean("TechStacks[0].is_deleted")).isFalse();
                softly.assertThat(response.body().jsonPath().getString("TechStacks[0].updated_at"))
                    .contains(techStack.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                softly.assertThat(response.body().jsonPath().getString("TechStacks[0].created_at"))
                    .contains(techStack.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        );
    }

    @Test
    @DisplayName("BCSDLab 트랙 정보 단건 조회 - 트랙에 속한 멤버와 기술스택이 없을 때")
    void findTrackWithEmptyMembersAndTechStacks() {
        Track track = Track.builder().name("BackEnd").build();
        trackRepository.save(track);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/tracks/{id}", track.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("TrackName")).isEqualTo(track.getName());
                softly.assertThat(response.body().jsonPath().getList("Members")).hasSize(0);
                softly.assertThat(response.body().jsonPath().getList("TechStacks")).hasSize(0);
            }
        );
    }
}
