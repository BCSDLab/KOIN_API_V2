package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.MemberAcceptanceFixture;
import in.koreatech.koin.acceptance.support.JsonAssertions;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.TrackRepository;

class MemberApiTest extends AcceptanceTest {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private MemberAcceptanceFixture memberFixture;

    Track backend;
    Track frontend;

    @BeforeAll
    void setUp() {
        clear();
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
            .andReturn();

        JsonAssertions.assertThat(result.getResponse().getContentAsString())
            .isEqualTo("""
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
                }"""
            );
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
