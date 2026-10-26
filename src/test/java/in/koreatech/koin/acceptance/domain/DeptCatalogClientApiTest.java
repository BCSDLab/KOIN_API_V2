package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.AcademicProgramYear;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.global.config.WebAuthProperties;
import jakarta.servlet.http.Cookie;

class DeptCatalogClientApiTest extends AcceptanceTest {

    private static final String ORIGIN = "http://localhost:3000";

    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private UserAcceptanceFixture userFixture;
    @Autowired private WebAuthProperties webAuthProperties;

    @BeforeEach
    void seedDepartments() {
        Department previousDesign = departmentRepository.save(new Department("디자인공학부"));
        Department currentDesign = departmentRepository.save(new Department("디자인공학과"));
        Department energy = departmentRepository.save(new Department("에너지신소재화학공학부"));
        entityManager.persist(new AcademicProgramYear("2025", previousDesign, null));
        entityManager.persist(new AcademicProgramYear("2026", currentDesign, null));
        entityManager.persist(new AcademicProgramYear("2026", energy, null));
    }

    @ParameterizedTest
    @CsvSource({
        "ANONYMOUS,2025510001", "ANONYMOUS,2026510001",
        "COOKIE,2025510001", "COOKIE,2026510001",
        "IOS,2025510001", "IOS,2026510001",
        "ANDROID,2025510001", "ANDROID,2026510001"
    })
    void 인증방식과_학번에_관계없이_같은_전체_학부를_기존_응답_형식으로_조회한다(
        String client, String studentNumber
    ) throws Exception {
        Student student = student(studentNumber);
        MockHttpServletRequestBuilder request = get("/depts");
        if (client.equals("COOKIE")) {
            request.header(HttpHeaders.ORIGIN, ORIGIN).cookie(login());
        } else if (client.equals("IOS")) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + userFixture.getToken(student.getUser()));
        } else if (client.equals("ANDROID")) {
            // 현재 Android는 이 목록을 로그인 토큰 없이 요청한다.
            request.header(HttpHeaders.USER_AGENT, "koin/1.0 Android");
        }

        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "name": "디자인공학부",
                        "curriculum_link": "https://www.koreatech.ac.kr/menu.es?mid=a10201000000",
                        "dept_nums": ["N/A"]
                    },
                    {
                        "name": "디자인공학과",
                        "curriculum_link": "https://www.koreatech.ac.kr/menu.es?mid=a10201000000",
                        "dept_nums": ["N/A"]
                    },
                    {
                        "name": "에너지신소재화학공학부",
                        "curriculum_link": "https://www.koreatech.ac.kr/menu.es?mid=a10201000000",
                        "dept_nums": ["N/A"]
                    }
                ]
                """, true));
    }

    @Test
    void 전체_목록에_있어도_학번에_맞지_않는_학부의_신규_저장은_거절한다() throws Exception {
        Student student = student("2026510001");
        mockMvc.perform(put("/user/student/academic-info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userFixture.getToken(student.getUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"student_number\":\"2026510001\",\"department\":\"디자인공학부\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 전체_목록에서_학번에_맞는_학부를_선택하면_저장한다() throws Exception {
        Student student = student("2026510001");
        mockMvc.perform(put("/user/student/academic-info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userFixture.getToken(student.getUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"student_number\":\"2026510001\",\"department\":\"디자인공학과\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.department").value("디자인공학과"));
    }

    private Student student(String studentNumber) {
        Student student = userFixture.준호_학생(null, null);
        student.updateStudentNumber(studentNumber);
        entityManager.flush();
        return student;
    }

    private Cookie login() throws Exception {
        MvcResult result = mockMvc.perform(post("/v2/web/auth/login")
                .header(HttpHeaders.ORIGIN, ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login_id\":\"juno\",\"login_pw\":\"1234\"}"))
            .andExpect(status().isCreated())
            .andReturn();
        return result.getResponse().getCookie(webAuthProperties.accessCookieName());
    }
}
