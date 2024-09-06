package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.dept.model.Dept;
import in.koreatech.koin.support.JsonAssertions;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

        MvcResult result = mockMvc.perform(
                get("/depts")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        JsonNode jsonNode = JsonAssertions.convertJsonNode(result);
        assertThat(jsonNode).hasSize(DEPT_SIZE);
    }
}
