package in.koreatech.koin.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;

class AdminUserApiTest extends AcceptanceTest {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @BeforeAll
    void setup() {
        clear();
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
}
