package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.dept.dto.DepartmentAndMajorResponse;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoRequest;
import in.koreatech.koin.domain.student.model.AcademicProgramYear;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AcademicProgramCohortCorrectionApiTest extends AcceptanceTest {

    private static final List<String> NEW_2026_DEPARTMENTS = List.of(
        "미래융합학부", "공학융합자율전공", "ICT융합자율전공", "사회융합자율전공", "반도체·디스플레이공학과");
    private static final Map<String, List<String>> CANONICAL_2025_PROGRAMS = Map.of(
        "디자인·건축공학부", List.of("디자인공학전공", "건축공학전공"),
        "에너지신소재화학공학부", List.of("에너지신소재공학전공", "화학생명공학전공"));
    private static final Map<String, String> LEGACY_DEPARTMENT_MAJORS = Map.of(
        "디자인공학부", "디자인공학전공", "건축공학부", "건축공학전공",
        "에너지신소재공학부", "에너지신소재공학전공", "화학생명공학부", "화학생명공학전공");

    @Autowired private UserAcceptanceFixture userFixture;
    @Autowired private StudentRepository studentRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private MajorRepository majorRepository;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private ObjectMapper objectMapper;

    private Student student;
    private String token;
    private Department computerDepartment;
    private Major computerMajor;
    private Department managementDepartment;
    private Major managementMajor;

    @BeforeEach
    void setup() {
        clear();
        transactionTemplate.executeWithoutResult(transaction -> {
            // 전체 요람 seed 대신 연도별 허용과 제외 경계를 검증하는 최소 DB fixture다.
            computerDepartment = departmentRepository.save(new Department("컴퓨터공학부"));
            computerMajor = majorRepository.save(new Major(null, computerDepartment));
            linkYear("2025", computerDepartment, computerMajor);
            Major newComputerMajor = majorRepository.save(new Major("컴퓨터공학전공", computerDepartment));
            linkYear("2026", computerDepartment, newComputerMajor);

            managementDepartment = departmentRepository.save(new Department("산업경영학부"));
            managementMajor = majorRepository.save(new Major("융합경영전공", managementDepartment));
            linkYear("2025", managementDepartment, managementMajor);

            CANONICAL_2025_PROGRAMS.forEach((name, majors) -> {
                Department department = departmentRepository.save(new Department(name));
                majors.forEach(majorName -> linkYear("2025", department,
                    majorRepository.save(new Major(majorName, department))));
            });
            LEGACY_DEPARTMENT_MAJORS.forEach((name, majorName) -> {
                Department department = departmentRepository.save(new Department(name));
                majorRepository.save(new Major(majorName, department));
            });

            for (String name : NEW_2026_DEPARTMENTS) {
                Department department = departmentRepository.save(new Department(name));
                linkYear("2026", department, null);
            }
            // 전역 DB에는 남아 있어도 해당 학번의 새 선택으로 허용하지 않는다.
            Department appliedChemistry = departmentRepository.save(new Department("응용화학공학부"));
            majorRepository.save(new Major("응용화학공학전공", appliedChemistry));
        });
        student = userFixture.준호_학생(computerDepartment, computerMajor);
        token = userFixture.getToken(student.getUser());
    }

    @AfterEach
    void cleanup() {
        clear();
    }

    @Test
    void 정정된_2025_연결만_목록에_반환한다() throws Exception {
        assertThat(departments("2025")).isEqualTo(Map.of(
            "컴퓨터공학부", List.of(), "산업경영학부", List.of("융합경영전공"),
            "디자인·건축공학부", CANONICAL_2025_PROGRAMS.get("디자인·건축공학부"),
            "에너지신소재화학공학부", CANONICAL_2025_PROGRAMS.get("에너지신소재화학공학부")));
    }

    @Test
    void 신설_학부는_2026_연결에만_포함되고_응용화학은_제외된다() throws Exception {
        Map<String, List<String>> departments = departments("2026");
        assertThat(departments).hasSize(6).containsEntry("컴퓨터공학부", List.of("컴퓨터공학전공"));
        NEW_2026_DEPARTMENTS.forEach(name -> assertThat(departments).containsEntry(name, List.of()));
        assertThat(departments).doesNotContainKey("응용화학공학부");
    }

    @ParameterizedTest
    @ValueSource(strings = {"미래융합학부", "공학융합자율전공", "ICT융합자율전공", "사회융합자율전공", "반도체·디스플레이공학과"})
    void 학번2025의_신설_학부_신규_선택을_거절하고_기존_학적을_보존한다(String department) throws Exception {
        update("2025136001", department, null, 400);
        assertAcademic("2019136135", computerDepartment.getId(), computerMajor.getId());
    }

    @Test
    void 학번2025의_유효한_학부와_전공_변경을_저장한다() throws Exception {
        update("2025136001", managementDepartment.getName(), managementMajor.getName(), 200);
        assertAcademic("2025136001", managementDepartment.getId(), managementMajor.getId());
    }

    @ParameterizedTest
    @CsvSource({
        "디자인·건축공학부, 디자인공학전공", "디자인·건축공학부, 건축공학전공",
        "에너지신소재화학공학부, 에너지신소재공학전공", "에너지신소재화학공학부, 화학생명공학전공"
    })
    void 학번2025의_요람상_통합_학부와_전공을_새로_선택한다(String departmentName, String majorName) throws Exception {
        Department department = departmentRepository.getByName(departmentName);
        Major major = majorRepository.getByNameAndDepartmentId(majorName, department.getId());

        update("2025136001", departmentName, majorName, 200);
        assertAcademic("2025136001", department.getId(), major.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"디자인공학부", "건축공학부", "에너지신소재공학부", "화학생명공학부"})
    void 학번2025의_호환용_학부_신규_선택을_거절한다(String department) throws Exception {
        update("2025136001", department, LEGACY_DEPARTMENT_MAJORS.get(department), 400);
        assertAcademic("2019136135", computerDepartment.getId(), computerMajor.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"디자인공학부", "건축공학부", "에너지신소재공학부", "화학생명공학부"})
    void 제외된_기존_학부의_재저장은_거절하되_학적을_자동_변경하지_않는다(String departmentName) throws Exception {
        Department department = departmentRepository.getByName(departmentName);
        Major major = majorRepository.getByNameAndDepartmentId(
            LEGACY_DEPARTMENT_MAJORS.get(departmentName), department.getId());
        transactionTemplate.executeWithoutResult(transaction -> studentRepository.getById(student.getId())
            .updateStudentAcademicInfo("2025136001", department, major));

        assertThat(departments("2025")).doesNotContainKey(departmentName);
        update("2025136001", departmentName, null, 400);
        assertAcademic("2025136001", department.getId(), major.getId());
    }

    @Test
    void 이미_저장된_2025_미래융합학부도_학적_저장_요청은_거절한다() throws Exception {
        Department future = departmentRepository.getByName("미래융합학부");
        transactionTemplate.executeWithoutResult(transaction ->
            studentRepository.getById(student.getId()).updateStudentAcademicInfo("2025136001", future, null));

        assertThat(departments("2025")).doesNotContainKey(future.getName());
        update("2025136001", future.getName(), null, 400);
        assertAcademic("2025136001", future.getId(), null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/user/student/me", "/v2/users/students/me"})
    void 일반_회원정보_수정도_등록된_연도의_잘못된_기존_학부를_거절한다(String path) throws Exception {
        Department future = departmentRepository.getByName("미래융합학부");
        transactionTemplate.executeWithoutResult(transaction ->
            studentRepository.getById(student.getId()).updateStudentAcademicInfo("2025136001", future, null));

        mockMvc.perform(put(path).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "student_number", "2025136001", "major", future.getName(),
                    "gender", 1, "name", "서정빈", "nickname", "duehee",
                    "phone_number", student.getUser().getPhoneNumber()))))
            .andExpect(status().isBadRequest());

        assertAcademic("2025136001", future.getId(), null);
    }

    @Test
    void 기존_학적_조회는_잘못된_소속을_자동_변경하지_않는다() throws Exception {
        Department future = departmentRepository.getByName("미래융합학부");
        transactionTemplate.executeWithoutResult(transaction ->
            studentRepository.getById(student.getId()).updateStudentAcademicInfo("2025136001", future, null));

        mockMvc.perform(get("/user/student/me/academic-info").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.student_number").value("2025136001"))
            .andExpect(jsonPath("$.department").value(future.getName()));

        assertAcademic("2025136001", future.getId(), null);
    }

    @Test
    void 같은_미래융합학부라도_학번만_2025로_바꾸면_거절한다() throws Exception {
        Department future = departmentRepository.getByName("미래융합학부");
        transactionTemplate.executeWithoutResult(transaction ->
            studentRepository.getById(student.getId()).updateStudentAcademicInfo("2026136001", future, null));

        update("2025136001", future.getName(), null, 400);

        assertAcademic("2026136001", future.getId(), null);
    }

    @Test
    void 잘못된_기존_학부를_사용자가_유효한_학부로_고치면_저장한다() throws Exception {
        Department future = departmentRepository.getByName("미래융합학부");
        transactionTemplate.executeWithoutResult(transaction ->
            studentRepository.getById(student.getId()).updateStudentAcademicInfo("2025136001", future, null));

        update("2025136001", computerDepartment.getName(), null, 200);

        assertAcademic("2025136001", computerDepartment.getId(), computerMajor.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"미래융합학부", "공학융합자율전공", "ICT융합자율전공", "사회융합자율전공", "반도체·디스플레이공학과"})
    void 학번2026의_신설_학부_신규_선택을_저장한다(String department) throws Exception {
        update("2026136001", department, null, 200);
        assertAcademic("2026136001", departmentRepository.getByName(department).getId(), null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025136001", "2026136001"})
    void 연도_연결이_없는_응용화학_신규_선택을_거절한다(String number) throws Exception {
        update(number, "응용화학공학부", "응용화학공학전공", 400);
        assertAcademic("2019136135", computerDepartment.getId(), computerMajor.getId());
    }

    private void linkYear(String year, Department department, Major major) {
        entityManager.persist(AcademicProgramYear.builder()
            .studentYear(year).department(department).major(major).build());
    }

    private Map<String, List<String>> departments(String year) throws Exception {
        String body = mockMvc.perform(get("/depts/major").param("student_year", year))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return Arrays.stream(objectMapper.readValue(body, DepartmentAndMajorResponse[].class))
            .collect(Collectors.toMap(DepartmentAndMajorResponse::department, DepartmentAndMajorResponse::majors));
    }

    private void update(String number, String department, String major, int expectedStatus) throws Exception {
        mockMvc.perform(put("/user/student/academic-info")
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateStudentAcademicInfoRequest(number, department, major))))
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
