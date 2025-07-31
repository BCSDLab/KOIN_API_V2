package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.dept.model.Dept;

class DeptApiTest extends AcceptanceTest {

    @Test
    void 학과_번호를_통해_학과_이름을_조회한다() throws Exception {
        Dept dept = Dept.COMPUTER_SCIENCE;
        mockMvc.perform(
                get("/dept")
                    .param("dept_num", dept.getNumbers().get(0))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "dept_num": "35",
                    "name": "컴퓨터공학부"
                }
                """));
    }

    @Test
    void 모든_학과_정보를_조회한다() throws Exception {
        //given
        final int DEPT_SIZE = Dept.values().length - 1;

        mockMvc.perform(
                get("/depts")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(DEPT_SIZE));
    }
}
