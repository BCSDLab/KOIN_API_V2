package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.domain.dept.dto.DepartmentAndMajorResponse;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoRequest;
import in.koreatech.koin.domain.student.model.AcademicProgramYear;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AcademicProgramYearApiTest extends AcceptanceTest {

    @Autowired private UserAcceptanceFixture userFixture;
    @Autowired private StudentRepository studentRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private MajorRepository majorRepository;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private StudentRedisRepository studentRedisRepository;
    @Autowired private UserRepository userRepository;

    private Student student;
    private String token;
    private Department computerDepartment;
    private Major legacyComputerMajor;
    private Department legacyManagementDepartment;
    private Major legacyManagementMajor;

    @BeforeEach
    void setup() {
        clear();
        transactionTemplate.executeWithoutResult(transaction -> {
            computerDepartment = departmentRepository.save(new Department("컴퓨터공학부"));
            legacyComputerMajor = majorRepository.save(new Major(null, computerDepartment));
            legacyManagementDepartment = departmentRepository.save(new Department("산업경영학부"));
            legacyManagementMajor = majorRepository.save(new Major("융합경영전공", legacyManagementDepartment));
            linkYear("2025", computerDepartment, legacyComputerMajor);
            linkYear("2025", legacyManagementDepartment, legacyManagementMajor);

            linkYear("2026", computerDepartment,
                majorRepository.save(new Major("컴퓨터공학전공", computerDepartment)));
            linkYear("2026", computerDepartment,
                majorRepository.save(new Major("AI·소프트웨어전공", computerDepartment)));
            Department management = departmentRepository.save(new Department("경영학부"));
            linkYear("2026", management, majorRepository.save(new Major("융합경영전공", management)));
        });
        student = userFixture.준호_학생(computerDepartment, legacyComputerMajor);
        token = userFixture.getToken(student.getUser());
    }

    @AfterEach
    void cleanup() {
        clear();
    }

    @Test
    void 연도_없는_기존_호출과_매핑이_없는_과거_학번의_목록을_유지한다() throws Exception {
        Map<String, List<String>> legacy = departments(null);
        assertThat(departments("2019")).isEqualTo(legacy);
        assertThat(departments("2024")).isEqualTo(legacy);
        assertThat(legacy).containsKeys("산업경영학부", "경영학부");
        assertThat(legacy.get("산업경영학부")).containsExactly("융합경영전공");
    }

    @Test
    void 학번에_맞는_학부와_전공_목록을_조회한다() throws Exception {
        Map<String, List<String>> previous = departments("2025");
        Map<String, List<String>> current = departments("2026");
        assertThat(previous.get("컴퓨터공학부")).isEmpty();
        assertThat(previous).containsKey("산업경영학부").doesNotContainKey("경영학부");
        assertThat(previous.get("산업경영학부")).containsExactly("융합경영전공");
        assertThat(current.get("컴퓨터공학부"))
            .containsExactly("컴퓨터공학전공", "AI·소프트웨어전공");
        assertThat(current).containsKey("경영학부")
            .doesNotContainKey("산업경영학부");
        assertThat(current.get("경영학부")).containsExactly("융합경영전공");
    }

    @Test
    void 학번_연도가_네_자리_숫자가_아니면_거절한다() throws Exception {
        mockMvc.perform(get("/depts/major").param("student_year", "26"))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2019136001", "2022136001", "2024136001", "2025136001", "2027136001"})
    void 연도_기준이_없거나_해당_기준에_맞는_기존_컴공_전공은_보존한다(String number) throws Exception {
        update(number, "컴퓨터공학부", null, 200);
        assertAcademic(number, computerDepartment.getId(), legacyComputerMajor.getId());
    }

    @Test
    void 매핑에서_빠진_기존_학부의_재저장은_거절하고_저장된_학적은_보존한다() throws Exception {
        changeExistingAcademic("2026136001", "산업경영학부", "융합경영전공");
        update("2026136001", "산업경영학부", null, 400);
        assertAcademic("2026136001", legacyManagementDepartment.getId(), legacyManagementMajor.getId());
    }

    @Test
    void 연도_기준이_있으면_기존_전공을_생략해서_재저장해도_검증한다() throws Exception {
        changeExistingAcademic("2025136001", "컴퓨터공학부", "AI·소프트웨어전공");
        Integer previousId = majorRepository.getByNameAndDepartmentId("AI·소프트웨어전공", computerDepartment.getId()).getId();
        update("2025136001", "컴퓨터공학부", null, 404);
        assertAcademic("2025136001", computerDepartment.getId(), previousId);
    }

    @Test
    void 연도_기준이_있으면_기존_이름_없는_전공도_검증한다() throws Exception {
        changeExistingAcademic("2026136001", "컴퓨터공학부", null);
        transactionTemplate.executeWithoutResult(transaction -> studentRepository.getById(student.getId())
            .updateStudentAcademicInfo("2026136001", computerDepartment, legacyComputerMajor));

        update("2026136001", "컴퓨터공학부", null, 404);

        assertAcademic("2026136001", computerDepartment.getId(), legacyComputerMajor.getId());
    }

    @Test
    void 해당_연도_연결이_전혀_없는_2027학번은_연도별_검증만_생략한다() throws Exception {
        assertThat(departments("2027")).isEqualTo(departments(null));

        update("2027136001", "컴퓨터공학부", "AI·소프트웨어전공", 200);

        Integer majorId = majorRepository.getByNameAndDepartmentId(
            "AI·소프트웨어전공", computerDepartment.getId()).getId();
        assertAcademic("2027136001", computerDepartment.getId(), majorId);
        update("2027136001", "존재하지않는학부", null, 400);
        update("2027136001", "컴퓨터공학부", "존재하지않는전공", 404);
        update("2027136001", "컴퓨터공학부", "융합경영전공", 404);
        update("2027", "컴퓨터공학부", null, 400);
        assertAcademic("2027136001", computerDepartment.getId(), majorId);
    }

    @Test
    void 요청_학부가_아닌_다른_학부만_등록돼도_그_연도는_검증한다() throws Exception {
        transactionTemplate.executeWithoutResult(transaction -> linkYear(
            "2027", computerDepartment, legacyComputerMajor));
        changeExistingAcademic("2027136001", "산업경영학부", "융합경영전공");

        update("2027136001", "산업경영학부", null, 400);

        assertAcademic("2027136001", legacyManagementDepartment.getId(), legacyManagementMajor.getId());
    }

    @Test
    void 기존_전공이라도_명시적으로_선택하면_학번에_맞는지_검증한다() throws Exception {
        changeExistingAcademic("2025136001", "컴퓨터공학부", "AI·소프트웨어전공");
        Integer previousId = majorRepository.getByNameAndDepartmentId(
            "AI·소프트웨어전공", computerDepartment.getId()).getId();

        update("2025136001", "컴퓨터공학부", "AI·소프트웨어전공", 404);

        assertAcademic("2025136001", computerDepartment.getId(), previousId);
    }

    @Test
    void 기존_NULL_전공_연결을_새_전공으로_채우지_않는다() throws Exception {
        changeExistingAcademic("2025136001", "컴퓨터공학부", null);
        update("2025136001", "컴퓨터공학부", null, 200);
        assertAcademic("2025136001", computerDepartment.getId(), null);
    }

    @Test
    void 해당_학번에_등록된_새_전공을_저장한다() throws Exception {
        update("2026136001", "컴퓨터공학부", "AI·소프트웨어전공", 200);
        Integer majorId = majorRepository.getByNameAndDepartmentId("AI·소프트웨어전공", computerDepartment.getId()).getId();
        assertAcademic("2026136001", computerDepartment.getId(), majorId);
    }

    @Test
    void 해당_학번에_없는_새_조합은_거절하고_기존_학적을_보존한다() throws Exception {
        update("2025136001", "컴퓨터공학부", "AI·소프트웨어전공", 404);
        assertAcademic("2019136135", computerDepartment.getId(), legacyComputerMajor.getId());
        update("2025136001", "경영학부", null, 400);
        assertAcademic("2019136135", computerDepartment.getId(), legacyComputerMajor.getId());
    }

    @Test
    void 새_학부에서_전공을_선택하지_않으면_이름_있는_첫_전공을_자동_지정하지_않는다() throws Exception {
        update("2026136001", "경영학부", null, 200);
        Integer departmentId = departmentRepository.getByName("경영학부").getId();
        assertAcademic("2026136001", departmentId, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"공학융합자율전공", "ICT융합자율전공", "사회융합자율전공", "반도체·디스플레이공학과", "미래융합학부"})
    void 전공_행이_없는_학부도_기존_프로필_API로_저장한다(String departmentName) throws Exception {
        transactionTemplate.executeWithoutResult(transaction -> {
            Department department = departmentRepository.save(new Department(departmentName));
            linkYear("2026", department, null);
        });
        assertThat(departments("2026").get(departmentName)).isEmpty();
        mockMvc.perform(put("/user/student/me")
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "gender", 1, "major", departmentName, "name", "서정빈", "nickname", "duehee",
                    "phone_number", "01023456789", "student_number", "2026136001"))))
            .andExpect(status().isOk());
        assertAcademic("2026136001", departmentRepository.getByName(departmentName).getId(), null);
    }

    @Test
    void 기존_프로필_API도_같은_학부의_선택된_전공을_보존한다() throws Exception {
        changeExistingAcademic("2026136001", "컴퓨터공학부", "AI·소프트웨어전공");
        Integer majorId = majorRepository.getByNameAndDepartmentId("AI·소프트웨어전공", computerDepartment.getId()).getId();
        mockMvc.perform(put("/user/student/me")
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "gender", 1, "major", "컴퓨터공학부", "name", "서정빈", "nickname", "duehee",
                    "phone_number", "01023456789", "student_number", "2026136001"))))
            .andExpect(status().isOk());
        assertAcademic("2026136001", computerDepartment.getId(), majorId);
    }

    @Test
    void 학부_없이_전공만_선택하면_기존_학적을_유지하며_거절한다() throws Exception {
        update("2026136001", null, "AI·소프트웨어전공", 400);
        assertAcademic("2019136135", computerDepartment.getId(), legacyComputerMajor.getId());
    }

    @Test
    void DB에만_추가한_신설_학부와_전공을_2027학번이_조회하고_저장한다() throws Exception {
        Department department = addDepartment("2027");
        Major major = transactionTemplate.execute(transaction -> {
            Major created = Major.builder().department(department).name("테스트신설전공").build();
            entityManager.persist(created);
            entityManager.persist(AcademicProgramYear.builder()
                .studentYear("2027").department(department).major(created).build());
            return created;
        });

        assertThat(departments("2027").get(department.getName())).containsExactly(major.getName());
        assertThat(departments("2025")).doesNotContainKey(department.getName());
        assertThat(departments("2026")).doesNotContainKey(department.getName());
        update("2027136001", department.getName(), major.getName(), 200);
        assertAcademic("2027136001", department.getId(), major.getId());
    }

    @Test
    void DB에만_추가한_신설_학부는_전공이_없어도_저장한다() throws Exception {
        Department department = addDepartment("2027");
        update("2027136001", department.getName(), null, 200);
        assertAcademic("2027136001", department.getId(), null);
    }

    @Test
    void DB에_있는_신설_학부라도_다른_학번에만_연결됐으면_변경을_거절한다() throws Exception {
        Department department = addDepartment("2027");
        update("2026136001", department.getName(), null, 400);
        assertAcademic("2019136135", computerDepartment.getId(), legacyComputerMajor.getId());
    }

    @Test
    void 연도_연결이_없는_과거_학번도_DB에_있는_학부를_사용한다() throws Exception {
        Department department = addDepartment("2027");
        update("2019136001", department.getName(), null, 200);
        assertAcademic("2019136001", department.getId(), null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/user/student/me", "/v2/users/students/me"})
    void 기존_프로필_API도_DB에만_추가한_신설_학부를_저장한다(String path) throws Exception {
        Department department = addDepartment("2027");
        mockMvc.perform(put(path)
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "gender", 1, "major", department.getName(), "name", "서정빈", "nickname", "duehee",
                    "phone_number", student.getUser().getPhoneNumber(), "student_number", "2027136001"))))
            .andExpect(status().isOk());
        assertAcademic("2027136001", department.getId(), null);
    }

    @Test
    void 관리자도_DB에만_추가한_신설_학부를_저장한다() throws Exception {
        Department department = addDepartment("2027");
        changeExistingAcademic("2019136135", "컴퓨터공학부", null);
        Admin admin = userFixture.코인_운영자();
        mockMvc.perform(put("/admin/users/student/{id}", student.getId())
                .header("Authorization", "Bearer " + userFixture.getToken(admin.getUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "gender", 1, "major", department.getName(), "name", "서정빈", "nickname", "duehee",
                    "phone_number", "01023456789", "student_number", "2027136001"))))
            .andExpect(status().isOk());
        assertAcademic("2027136001", department.getId(), null);
    }

    @Test
    void 관리자가_DB에_없는_학부를_저장하면_기존_학적을_보존하며_거절한다() throws Exception {
        Admin admin = userFixture.코인_운영자();
        mockMvc.perform(put("/admin/users/student/{id}", student.getId())
                .header("Authorization", "Bearer " + userFixture.getToken(admin.getUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "gender", 1, "major", "DB에없는학부", "name", "서정빈", "nickname", "duehee",
                    "phone_number", "01023456789", "student_number", "2027136001"))))
            .andExpect(status().isBadRequest());
        assertAcademic("2019136135", computerDepartment.getId(), legacyComputerMajor.getId());
    }

    @Test
    void 기존_이메일_회원가입도_DB에만_추가한_신설_학부를_사용한다() throws Exception {
        Department department = addDepartment("2026");
        String email = "newdept@koreatech.ac.kr";
        mockMvc.perform(post("/user/student/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "major", department.getName(), "email", email, "name", "김철수", "nickname", "newdept",
                    "password", "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                    "gender", 0, "is_graduated", false, "student_number", "2026136001",
                    "phone_number", "01000000000"))))
            .andExpect(status().isOk());
        String authToken = studentRedisRepository.findById(email).orElseThrow().getAuthToken();
        mockMvc.perform(get("/user/authenticate").queryParam("auth_token", authToken))
            .andExpect(status().isOk());
        transactionTemplate.executeWithoutResult(transaction -> {
            Integer id = userRepository.getByEmailAndUserTypeIn(email, UserType.KOIN_USER_TYPES).getId();
            Student registered = studentRepository.getById(id);
            assertThat(registered.getDepartment().getId()).isEqualTo(department.getId());
            assertThat(registered.getMajor()).isNull();
        });
    }

    @Test
    void 학부를_비우는_기존_요청은_계속_허용한다() throws Exception {
        update("2019136135", null, null, 200);
        transactionTemplate.executeWithoutResult(transaction -> {
            Student saved = studentRepository.getById(student.getId());
            assertThat(saved.getDepartment()).isNull();
            assertThat(saved.getMajor()).isNull();
        });
    }

    private Department addDepartment(String year) {
        return transactionTemplate.execute(transaction -> {
            Department department = departmentRepository.save(Department.builder().name("테스트신설학부").build());
            entityManager.persist(AcademicProgramYear.builder().studentYear(year).department(department).build());
            return department;
        });
    }

    private void linkYear(String year, Department department, Major major) {
        entityManager.persist(AcademicProgramYear.builder()
            .studentYear(year).department(department).major(major).build());
    }

    private Map<String, List<String>> departments(String year) throws Exception {
        MockHttpServletRequestBuilder request = get("/depts/major");
        if (year != null) {
            request.param("student_year", year);
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return Arrays.stream(objectMapper.readValue(body, DepartmentAndMajorResponse[].class))
            .collect(Collectors.toMap(DepartmentAndMajorResponse::department, DepartmentAndMajorResponse::majors));
    }

    private void changeExistingAcademic(String number, String departmentName, String majorName) {
        transactionTemplate.executeWithoutResult(transaction -> {
            Department department = departmentRepository.getByName(departmentName);
            Major major = majorName == null ? null
                : majorRepository.getByNameAndDepartmentId(majorName, department.getId());
            studentRepository.getById(student.getId()).updateStudentAcademicInfo(number, department, major);
        });
    }

    private void update(String number, String departmentName, String majorName, int expectedStatus) throws Exception {
        mockMvc.perform(put("/user/student/academic-info")
            .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new UpdateStudentAcademicInfoRequest(number, departmentName, majorName))))
            .andExpect(status().is(expectedStatus));
    }

    private void assertAcademic(String number, Integer departmentId, Integer majorId) {
        transactionTemplate.executeWithoutResult(transaction -> {
            Student saved = studentRepository.getById(student.getId());
            assertThat(saved.getStudentNumber()).isEqualTo(number);
            assertThat(saved.getDepartment().getId()).isEqualTo(departmentId);
            if (majorId == null) {
                assertThat(saved.getMajor()).isNull();
            } else {
                assertThat(saved.getMajor().getId()).isEqualTo(majorId);
            }
        });
    }
}
