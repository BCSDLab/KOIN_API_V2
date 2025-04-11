package in.koreatech.koin.admin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.DepartmentFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;

@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AdminUserApiTest extends AcceptanceTest {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private DepartmentFixture departmentFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 관리자가_로그인_한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String email = adminUser.getUser().getEmail();
        String password = "1234";

        mockMvc.perform(
                post("/admin/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자가_로그인_한다_관리자가_아니면_404_반환() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.준호_학생(department, null);
        String email = student.getUser().getEmail();
        String password = "1234";

        mockMvc.perform(
                post("/admin/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 인증_받지_못한_관리자가_로그인_한다() throws Exception {
        Admin admin = userFixture.진구_운영자();
        String email = admin.getUser().getEmail();
        String password = "1234";

        mockMvc.perform(
                post("/admin/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 관리자가_로그아웃한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                post("/admin/user/logout")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 관리자가_액세스_토큰_재발급_한다() throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        User user = adminUser.getUser();
        String email = user.getEmail();
        String password = "1234";
        String token = userFixture.getToken(user);

        MvcResult result = mockMvc.perform(
                post("/admin/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode loginJsonNode = JsonAssertions.convertJsonNode(result);

        mockMvc.perform(
                post("/admin/user/refresh")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "refresh_token" : "%s"
                        }
                        """.formatted(loginJsonNode.get("refresh_token").asText()))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자가_회원을_조회한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.준호_학생(department, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/users/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("준호"))
            .andExpect(jsonPath("$.name").value("테스트용_준호"))
            .andExpect(jsonPath("$.phoneNumber").value("01012345678"))
            .andExpect(jsonPath("$.email").value("juno@koreatech.ac.kr"));
    }

    @Test
    void 관리자가_회원을_삭제한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.준호_학생(department, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                delete("/admin/users/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        assertThat(adminUserRepository.findById(student.getId())).isNotPresent();
    }

    @Test
    void 관리자_계정_생성_권한이_있는_관리자가_관리자_계정을_만든다() throws Exception {
        Admin admin = userFixture.코인_운영자();
        String token = userFixture.getToken(admin.getUser());

        mockMvc.perform(
                post("/admin")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                          "email": "koin01234@koreatech.ac.kr",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "name": "신관규",
                          "track_type": "BACKEND",
                          "team_type": "USER"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자_계정_생성_권한이_없는_관리자가_관리자_계정을_만든다() throws Exception {
        Admin admin = userFixture.영희_운영자();
        String token = userFixture.getToken(admin.getUser());

        mockMvc.perform(
                post("/admin")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                          "email": "koin12345@koreatech.ac.kr",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "name": "신관규",
                          "track_type": "BACKEND",
                          "team_type": "USER"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 조건에_맞지_않는_이메일로_관리자_계정을_만든다() throws Exception {
        Admin admin = userFixture.코인_운영자();
        String token = userFixture.getToken(admin.getUser());

        mockMvc.perform(
                post("/admin")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                          "account": "admin123456@koreatech.ac.kr",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "name": "신관규",
                          "track_type": "BACKEND",
                          "team_type": "USER"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 관리자가_특정_관리자의_계정을_조회한다() throws Exception {
        Admin admin = userFixture.코인_운영자();
        String token = userFixture.getToken(admin.getUser());

        Admin admin1 = userFixture.영희_운영자();

        mockMvc.perform(
                get("/admin/{id}", admin1.getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                    "id": %d,
                    "email": "koinadmin1@koreatech.ac.kr",
                    "name": "테스트용_코인운영자",
                    "track_name": "Backend",
                    "team_name": "Business",
                    "role": "레귤러"
                }
                """, admin1.getId())));
    }

    @Test
    void 관리자가_관리자_계정_리스트를_조회한다() throws Exception {
        Admin admin = userFixture.코인_운영자();
        String token = userFixture.getToken(admin.getUser());

        Admin admin1 = userFixture.영희_운영자();
        Admin admin2 = userFixture.진구_운영자();

        mockMvc.perform(
                get("/admins")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                  "total_count": 3,
                  "current_count": 3,
                  "total_page": 1,
                  "current_page": 1,
                  "admins": [
                    {
                      "id": %d,
                      "email": "juno@koreatech.ac.kr",
                      "name": "테스트용_코인운영자",
                      "team_name": "User",
                      "track_name": "Backend",
                      "role": "트랙장"
                    },
                    {
                      "id": %d,
                      "email": "koinadmin1@koreatech.ac.kr",
                      "name": "테스트용_코인운영자",
                      "team_name": "Business",
                      "track_name": "Backend",
                      "role": "레귤러"
                    },
                    {
                      "id": %d,
                      "email": "koinadmin2@koreatech.ac.kr",
                      "name": "테스트용_코인운영자",
                      "team_name": "Campus",
                      "track_name": "Backend",
                      "role": "회장"
                    }
                  ]
                }
                """, admin.getId(), admin1.getId(), admin2.getId())));
    }
}
