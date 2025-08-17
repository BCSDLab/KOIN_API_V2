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
import in.koreatech.koin.acceptance.fixture.MemberAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.TrackAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.member.repository.AdminMemberRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.member.model.Member;

public class AdminMemberApiTest extends AcceptanceTest {

    @Autowired
    private MemberAcceptanceFixture memberFixture;

    @Autowired
    private TrackAcceptanceFixture trackFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private AdminMemberRepository adminMemberRepository;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void BCSDLab_회원들의_정보를_조회한다() throws Exception {
        memberFixture.최준호(trackFixture.backend());

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/members")
                    .header("Authorization", "Bearer " + token)
                    .param("page", "1")
                    .param("track", "BACKEND")
                    .param("is_deleted", "false")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }

    @Test
    void 관리자_권한으로_BCSDLab_회원을_추가한다() throws Exception {
        trackFixture.backend();

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

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

        mockMvc.perform(
                post("/admin/members")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isCreated());

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
    void BCSDLab_회원_정보를_조회한다() throws Exception {
        memberFixture.최준호(trackFixture.backend());

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/members/{id}", 1)
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }

    @Test
    void BCSDLab_회원_정보를_삭제한다() throws Exception {
        Member member = memberFixture.최준호(trackFixture.backend());
        Integer memberId = member.getId();

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                delete("/admin/members/{id}", memberId)
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk());

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

    @Test
    void BCSDLab_회원_정보를_수정한다() throws Exception {
        Member member = memberFixture.최준호(trackFixture.backend());
        Integer memberId = member.getId();

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        String jsonBody = """
            {
                "name": "최준호",
                "student_number": "2019136135",
                "track": "BackEnd",
                "position": "Mentor",
                "email": "testjuno@gmail.com",
                "image_url": "https://imagetest.com/juno.jpg"
            }
            """;

        mockMvc.perform(
                put("/admin/members/{id}", memberId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isOk());

        Member updatedMember = adminMemberRepository.getById(memberId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedMember.getName()).isEqualTo("최준호");
            softly.assertThat(updatedMember.getStudentNumber()).isEqualTo("2019136135");
            softly.assertThat(updatedMember.getTrack().getName()).isEqualTo("BackEnd");
            softly.assertThat(updatedMember.getPosition()).isEqualTo("Mentor");
            softly.assertThat(updatedMember.getEmail()).isEqualTo("testjuno@gmail.com");
            softly.assertThat(updatedMember.getImageUrl()).isEqualTo("https://imagetest.com/juno.jpg");
            softly.assertThat(updatedMember.isDeleted()).isEqualTo(false);
        });
    }

    @Test
    void BCSDLab_회원_정보를_트랙과_함께_수정한다() throws Exception {
        Member member = memberFixture.최준호(trackFixture.backend());
        trackFixture.frontend();
        Integer memberId = member.getId();

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        String jsonBody = """
            {
                "name": "최준호",
                "student_number": "2019136135",
                "track": "FrontEnd",
                "position": "Mentor",
                "email": "testjuno@gmail.com",
                "image_url": "https://imagetest.com/juno.jpg"
            }
            """;

        mockMvc.perform(
                put("/admin/members/{id}", memberId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isOk());

        Member updatedMember = adminMemberRepository.getById(memberId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedMember.getName()).isEqualTo("최준호");
            softly.assertThat(updatedMember.getStudentNumber()).isEqualTo("2019136135");
            softly.assertThat(updatedMember.getTrack().getName()).isEqualTo("FrontEnd");
            softly.assertThat(updatedMember.getPosition()).isEqualTo("Mentor");
            softly.assertThat(updatedMember.getEmail()).isEqualTo("testjuno@gmail.com");
            softly.assertThat(updatedMember.getImageUrl()).isEqualTo("https://imagetest.com/juno.jpg");
            softly.assertThat(updatedMember.isDeleted()).isEqualTo(false);
        });
    }

    @Test
    void BCSDLab_회원_정보를_삭제를_취소한다() throws Exception {
        Member member = memberFixture.최준호_삭제(trackFixture.backend());
        Integer memberId = member.getId();

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                post("/admin/members/{id}/undelete", memberId)
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk());

        Member savedMember = adminMemberRepository.getById(memberId);

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
}
