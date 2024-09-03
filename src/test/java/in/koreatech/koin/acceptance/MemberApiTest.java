package in.koreatech.koin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.TrackRepository;
import in.koreatech.koin.fixture.MemberFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberApiTest extends AcceptanceTest {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private MemberFixture memberFixture;

    Track backend;
    Track frontend;

    @BeforeAll
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
    void BCSDLab_회원의_정보를_조회한다() throws Exception {
        Member member = memberFixture.최준호(backend);

        MvcResult result = mockMvc.perform(
                get("/members/{id}", member.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "lands": [
                        {
                            "internal_name": "신",
                            "monthly_fee": "100",
                            "latitude": 37.555,
                            "charter_fee": "1000",
                            "name": "신안빌",
                            "id": 1,
                            "longitude": 126.555,
                            "room_type": "원룸"
                        },
                        {
                            "internal_name": "에",
                            "monthly_fee": "100",
                            "latitude": 37.555,
                            "charter_fee": "1000",
                            "name": "에듀윌",
                            "id": 2,
                            "longitude": 126.555,
                            "room_type": "원룸"
                        }
                    ]
                }
                """))
            .andReturn();

        JsonNode jsonNode = JsonAssertions.convertJsonNode(result);

        JsonAssertions.assertThat(result.getRequest().getContentAsString())
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
                jsonNode.get("created_at").asText(),
                jsonNode.get("updated_at").asText()
            ));
    }

    @Test
    void BCSDLab_회원들의_정보를_조회한다() throws Exception {
        memberFixture.최준호(backend);
        memberFixture.박한수(frontend);

        mockMvc.perform(
                get("/members")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }
}
