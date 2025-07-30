package in.koreatech.koin.acceptance.admin;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.student.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;

public class AdminStudentApiTest extends AcceptanceTest {

    @Autowired
    private AdminStudentRepository adminStudentRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 관리자가_학생_리스트를_파라미터가_없이_조회한다_페이지네이션() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.준호_학생(department, null);
        Admin adminUser = userFixture.코인_운영자();

        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/students")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "current_count": 1,
                     "current_page": 1,
                     "students": [
                         {
                             "email": "juno@koreatech.ac.kr",
                             "id": 1,
                             "major": "컴퓨터공학부",
                             "name": "테스트용_준호",
                             "nickname": "준호",
                             "student_number": "2019136135"
                         }
                     ],
                     "total_count": 1,
                     "total_page": 1
                 }
                """));
    }

    @Test
    void 관리자가_학생_리스트를_페이지_수와_limits으로_조회한다_페이지네이션() throws Exception {
        for (int i = 0; i < 11; i++) {
            Department department = departmentFixture.컴퓨터공학부();
            Student student = Student.builder()
                .studentNumber("2019136135")
                .department(department)
                .userIdentity(UNDERGRADUATE)
                .isGraduated(false)
                .user(
                    User.builder()
                        .loginPw(passwordEncoder.encode("1234"))
                        .name("테스트용_성재" + i)
                        .nickname("성재" + i)
                        .anonymousNickname("익명" + i)
                        .phoneNumber(String.format("0101234%04d", i))
                        .userType(STUDENT)
                        .gender(MAN)
                        .email("seongjae" + i + "@koreatech.ac.kr")
                        .loginId("seongjae" + i)
                        .isAuthed(true)
                        .isDeleted(false)
                        .build()
                )
                .build();

            adminStudentRepository.save(student);
        }

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/students")
                    .header("Authorization", "Bearer " + token)
                    .param("page", "2")
                    .param("limit", "10")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "current_count": 1,
                     "current_page": 2,
                     "students": [
                         {
                             "email": "seongjae10@koreatech.ac.kr",
                             "id": 11,
                             "major": "컴퓨터공학부",
                             "name": "테스트용_성재10",
                             "nickname": "성재10",
                             "student_number": "2019136135"
                         }
                     ],
                     "total_count": 11,
                     "total_page": 2
                 }
                """));
    }

    @Test
    void 관리자가_학생_리스트를_닉네임으로_조회한다_페이지네이션() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();

        Student student1 = userFixture.성빈_학생(department);
        Student student = userFixture.준호_학생(department, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/students")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("nickname", "준호")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "current_count": 1,
                     "current_page": 1,
                     "students": [
                         {
                             "email": "juno@koreatech.ac.kr",
                             "id": 2,
                             "major": "컴퓨터공학부",
                             "name": "테스트용_준호",
                             "nickname": "준호",
                             "student_number": "2019136135"
                         }
                     ],
                     "total_count": 1,
                     "total_page": 1
                 }
                """));
    }

    @Test
    void 관리자가_특정_학생_정보를_조회한다_관리자가_아니면_403_반환() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.준호_학생(department, null);
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                get("/admin/users/student/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 관리자가_특정_학생_정보를_조회한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.준호_학생(department, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/users/student/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "anonymous_nickname": "익명_주노",
                    "created_at": "2024-01-15 12:00:00",
                    "email": "juno@koreatech.ac.kr",
                    "gender": 0,
                    "id": 1,
                    "is_authed": true,
                    "is_graduated": false,
                    "last_logged_at": null,
                    "major": "컴퓨터공학부",
                    "name": "테스트용_준호",
                    "nickname": "준호",
                    "phone_number": "01012345678",
                    "student_number": "2019136135",
                    "updated_at": "2024-01-15 12:00:00",
                    "user_type": "STUDENT"
                }
                """));
    }

    @Test
    void 관리자가_특정_학생_정보를_수정한다() throws Exception {
        departmentFixture.전체학부();

        Department department = departmentRepository.getByName("컴퓨터공학부");
        Student student = userFixture.준호_학생(department, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                put("/admin/users/student/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                          {
                            "gender" : 1,
                            "major" : "기계공학부",
                            "name" : "서정빈",
                            "password" : "0c4be6acaba1839d3433c1ccf04e1eec4d1fa841ee37cb019addc269e8bc1b77",
                            "nickname" : "duehee",
                            "phone_number" : "01023456789",
                            "student_number" : "2019136136"
                          }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "anonymous_nickname": "익명_주노",
                    "email": "juno@koreatech.ac.kr",
                    "gender": 1,
                    "major": "기계공학부",
                    "name": "서정빈",
                    "nickname": "duehee",
                    "phone_number": "01023456789",
                    "student_number": "2019136136"
                }
                """));

        transactionTemplate.executeWithoutResult(status -> {
            Student result = adminStudentRepository.getById(student.getId());
            SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(result.getUser().getName()).isEqualTo("서정빈");
                    softly.assertThat(result.getUser().getNickname()).isEqualTo("duehee");
                    softly.assertThat(result.getUser().getGender()).isEqualTo(UserGender.from(1));
                    softly.assertThat(result.getStudentNumber()).isEqualTo("2019136136");
                }
            );
        });
    }
}
