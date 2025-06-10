package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.acceptance.fixture.MemberAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.TechStackAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.TrackAcceptanceFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrackApiTest extends AcceptanceTest {

    @Autowired
    private TrackAcceptanceFixture trackFixture;

    @Autowired
    private MemberAcceptanceFixture memberFixture;

    @Autowired
    private TechStackAcceptanceFixture techStackFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void BCSDLab_트랙_정보를_조회한다() throws Exception {
        trackFixture.backend();
        trackFixture.frontend();
        trackFixture.ios();

        mockMvc.perform(
                get("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }

    @Test
    void BCSDLab_트랙_정보_단건_조회_삭제된_멤버는_조회하지_않는다() throws Exception {
        Track track = trackFixture.backend();
        memberFixture.배진호(track); // 삭제된 멤버
        memberFixture.최준호(track);
        techStackFixture.java(track);

        mockMvc.perform(
                get("/tracks/{id}", track.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                     ]
                 }
                """));
    }

    @Test
    void BCSDLab_트랙_정보_단건_조회() throws Exception {
        Track track = trackFixture.backend();
        memberFixture.최준호(track);
        techStackFixture.java(track);

        mockMvc.perform(
                get("/tracks/{id}", track.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }

    @Test
    void BCSDLab_트랙_정보_단건_조회_트랙에_속한_멤버와_기술스택이_없을_때() throws Exception {
        Track track = trackFixture.frontend();

        mockMvc.perform(
                get("/tracks/{id}", track.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "TrackName": "FrontEnd",
                    "TechStacks": [
                      
                    ],
                    "Members": [
                       
                    ]
                }
                """));
    }
}
