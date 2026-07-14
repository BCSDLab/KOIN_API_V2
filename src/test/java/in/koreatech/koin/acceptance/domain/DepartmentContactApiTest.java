package in.koreatech.koin.acceptance.domain;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContact;

class DepartmentContactApiTest extends AcceptanceTest {

    @Test
    void 전체_연락처를_조회하고_부서명으로_필터링한다() throws Exception {
        clear();
        entityManager.persist(new DepartmentContact("교무팀", "", "041-560-2524"));
        entityManager.persist(new DepartmentContact("학사팀", "교육과정", "041-560-2527"));
        entityManager.persist(new DepartmentContact("학사팀", "수업", "041-560-2528"));
        entityManager.flush();

        mockMvc.perform(get("/department-contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].department").value("교무팀"))
            .andExpect(jsonPath("$[0].task").value(""))
            .andExpect(jsonPath("$[0].phone_number").value("041-560-2524"));

        mockMvc.perform(
                get("/department-contacts")
                    .param("department", " 학사팀 ")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].department").value("학사팀"))
            .andExpect(jsonPath("$[0].task").value("교육과정"))
            .andExpect(jsonPath("$[0].phone_number").value("041-560-2527"))
            .andExpect(jsonPath("$[1].task").value("수업"))
            .andExpect(jsonPath("$[1].phone_number").value("041-560-2528"));
    }
}
