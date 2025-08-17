package in.koreatech.koin.acceptance.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.MemberAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.TechStackAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.TrackAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.member.repository.AdminTechStackRepository;
import in.koreatech.koin.admin.member.repository.AdminTrackRepository;
import in.koreatech.koin.admin.operators.model.Admin;
import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;

public class AdminTrackApiTest extends AcceptanceTest {

    @Autowired
    private TrackAcceptanceFixture trackFixture;

    @Autowired
    private MemberAcceptanceFixture memberFixture;

    @Autowired
    private TechStackAcceptanceFixture techStackFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private AdminTrackRepository adminTrackRepository;

    @Autowired
    private AdminTechStackRepository adminTechStackRepository;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 관리자가_BCSDLab_트랙_정보를_조회한다_관리자가_아니면_403_반환() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.준호_학생(department, null);
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                get("/admin/tracks")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 관리자가_BCSDLab_트랙_정보를_조회한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        trackFixture.backend();
        trackFixture.frontend();
        trackFixture.ios();

        mockMvc.perform(
                get("/admin/tracks")
                    .header("Authorization", "Bearer " + token)
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
    void 관리자가_BCSDLab_트랙_정보를_생성한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                post("/admin/tracks")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "BackEnd",
                          "headcount": 20
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "id": 1,
                    "name": "BackEnd",
                    "headcount": 20,
                    "is_deleted": false,
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """));
    }

    @Test
    void 관리자가_BCSDLab_트랙_정보를_생성한다_이미_있는_트랙명이면_409_반환() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        trackFixture.backend();

        mockMvc.perform(
                post("/admin/tracks")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "BackEnd",
                          "headcount": 20
                        }
                        """)
            )
            .andExpect(status().isConflict());
    }

    @Test
    void 관리자가_BCSDLab_트랙_단거_정보를_조회한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        Track backend = trackFixture.backend();
        trackFixture.ai();                    // 삭제된 트랙
        memberFixture.배진호(backend);         // 삭제된 멤버
        memberFixture.최준호(backend);
        techStackFixture.java(backend);
        techStackFixture.adobeFlash(backend); //삭제된 기술스택

        mockMvc.perform(
                get("/admin/tracks/{id}", backend.getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }

    @Test
    void 관리자가_BCSDLab_트랙_정보를_수정한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        Track backEnd = trackFixture.backend();

        mockMvc.perform(
                put("/admin/tracks/{id}", backEnd.getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "frontEnd",
                          "headcount": 20
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "id": 1,
                    "name": "frontEnd",
                    "headcount": 20,
                    "is_deleted": false,
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """));
    }

    @Test
    void 관리자가_BCSDLab_트랙_정보를_수정한다_이미_있는_트랙명이면_409_반환() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        Track backEnd = trackFixture.backend();

        mockMvc.perform(
                put("/admin/tracks/{id}", backEnd.getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "BackEnd",
                          "headcount": 20
                        }
                        """)
            )
            .andExpect(status().isConflict());
    }

    @Test
    void 관리자가_BCSDLab_트랙_정보를_삭제한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        Track backEnd = trackFixture.backend();

        mockMvc.perform(
                delete("/admin/tracks/{id}", backEnd.getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk());

        Track updatedTrack = adminTrackRepository.getById(backEnd.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedTrack.getName()).isEqualTo(backEnd.getName());
            softly.assertThat(updatedTrack.getHeadcount()).isEqualTo(backEnd.getHeadcount());
            softly.assertThat(updatedTrack.isDeleted()).isEqualTo(true);
        });
    }

    @Test
    void 관리자가_BCSDLab_기술스택_정보를_생성한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        trackFixture.frontend();
        Track backEnd = trackFixture.backend();

        mockMvc.perform(
                post("/admin/techStacks")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "image_url": "https://url.com",
                            "name": "Spring",
                            "description": "스프링은 웹 프레임워크이다"
                        }
                        """)
                    .param("trackName", backEnd.getName())
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }

    @Test
    void 관리자가_BCSDLab_기술스택_정보를_수정한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        TechStack java = techStackFixture.java(trackFixture.frontend());
        Track backEnd = trackFixture.backend();

        mockMvc.perform(
                put("/admin/techStacks/{id}", java.getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "image_url": "https://java.com",
                            "name": "JAVA",
                            "description": "java의 TrackID를 BackEnd로 수정한다.",
                            "is_deleted": true
                        }
                        """)
                    .param("trackName", backEnd.getName())
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }

    @Test
    void 관리자가_기술스택_정보를_삭제한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        Track backEnd = trackFixture.backend();
        TechStack java = techStackFixture.java(backEnd);

        mockMvc.perform(
                delete("/admin/techStacks/{id}", java.getId())
                    .header("Authorization", "Bearer " + token)

            )
            .andExpect(status().isOk());

        TechStack updatedtechStack = adminTechStackRepository.getById(java.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedtechStack.getImageUrl()).isEqualTo(java.getImageUrl());
            softly.assertThat(updatedtechStack.getName()).isEqualTo(java.getName());
            softly.assertThat(updatedtechStack.getDescription()).isEqualTo(java.getDescription());
            softly.assertThat(updatedtechStack.getTrackId()).isEqualTo(backEnd.getId());
            softly.assertThat(updatedtechStack.isDeleted()).isEqualTo(true);
        });
    }
}
