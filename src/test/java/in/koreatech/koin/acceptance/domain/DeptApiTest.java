package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.dept.model.Dept;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;

class DeptApiTest extends AcceptanceTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 학부_목록의_공개_응답_구조와_요청_형식을_유지한다() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode openApi = objectMapper.readTree(body);
        JsonNode operation = openApi.path("paths").path("/depts").path("get");
        assertThat(operation.path("parameters").size()).isZero();
        JsonNode schema = operation.path("responses").path("200").path("content")
            .elements().next().path("schema");
        assertThat(schema.path("type").asText()).isEqualTo("array");
        assertThat(schema.path("items").path("$ref").asText()).isEqualTo("#/components/schemas/DeptsResponse");
        JsonNode properties = openApi.path("components").path("schemas").path("DeptsResponse").path("properties");
        List<String> fields = new ArrayList<>();
        properties.fieldNames().forEachRemaining(fields::add);
        assertThat(fields).containsExactlyInAnyOrder("name", "curriculum_link", "dept_nums");
        assertThat(properties.path("name").path("type").asText()).isEqualTo("string");
        assertThat(properties.path("curriculum_link").path("type").asText()).isEqualTo("string");
        assertThat(properties.path("dept_nums").path("type").asText()).isEqualTo("array");
        assertThat(properties.path("dept_nums").path("items").path("type").asText()).isEqualTo("string");
    }

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
    void DB에_등록된_신규_학부까지_기존_응답_형식으로_조회한다() throws Exception {
        departmentRepository.save(new Department("응용화학공학부"));
        departmentRepository.save(new Department("테스트신설학부"));

        mockMvc.perform(
                get("/depts")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "name": "응용화학공학부",
                        "curriculum_link": "https://www.koreatech.ac.kr/menu.es?mid=a10201000000",
                        "dept_nums": ["N/A"]
                    },
                    {
                        "name": "테스트신설학부",
                        "curriculum_link": "https://www.koreatech.ac.kr/menu.es?mid=a10201000000",
                        "dept_nums": ["N/A"]
                    }
                ]
                """, true));
    }

    @Test
    void DB에_학부가_없으면_빈_목록을_반환한다() throws Exception {
        mockMvc.perform(get("/depts"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]", true));
    }

    @Test
    void DB의_링크와_학과번호_노출여부_표시순서를_반영한다() throws Exception {
        entityManager.createNativeQuery("""
            INSERT INTO department
                (name, curriculum_link, dept_nums, is_selectable, display_order, created_at, updated_at)
            VALUES
                ('응용화학공학부', 'https://example.com/applied', '["91"]', 1, 20, NOW(), NOW()),
                ('컴퓨터공학부', 'https://example.com/computer', '["35", "36"]', 1, 10, NOW(), NOW()),
                ('학과공통', 'https://example.com/common', '["N/A"]', 0, 1, NOW(), NOW()),
                ('에너지신소재화학공학부', 'https://example.com/legacy', '["74"]', 0, 2, NOW(), NOW())
            """).executeUpdate();
        entityManager.clear();

        mockMvc.perform(get("/depts"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "name": "컴퓨터공학부",
                        "curriculum_link": "https://example.com/computer",
                        "dept_nums": ["35", "36"]
                    },
                    {
                        "name": "응용화학공학부",
                        "curriculum_link": "https://example.com/applied",
                        "dept_nums": ["91"]
                    }
                ]
                """, true));
    }

    @Test
    void DB의_표시여부를_바꾸면_학부명과_관계없이_목록에_반영한다() throws Exception {
        Department department = departmentRepository.save(new Department("학과공통"));
        entityManager.flush();

        mockMvc.perform(get("/depts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("학과공통"));

        entityManager.createNativeQuery("UPDATE department SET is_selectable = 0 WHERE id = :id")
            .setParameter("id", department.getId())
            .executeUpdate();
        entityManager.clear();

        mockMvc.perform(get("/depts"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]", true));
    }
}
