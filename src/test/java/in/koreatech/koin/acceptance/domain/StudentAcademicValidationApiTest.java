package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.dto.RegisterStudentRequest;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoRequest;
import in.koreatech.koin.domain.student.model.AcademicProgramYear;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StudentAcademicValidationApiTest extends AcceptanceTest {

    @Autowired private UserAcceptanceFixture userFixture;
    @Autowired private StudentRepository studentRepository;
    @Autowired private StudentRedisRepository studentRedisRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private MajorRepository majorRepository;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private ObjectMapper objectMapper;

    private Department computer;
    private Department future;
    private Major computerMajor;
    private Student student;
    private String token;

    @BeforeEach
    void setup() {
        clear();
        transactionTemplate.executeWithoutResult(transaction -> {
            computer = departmentRepository.save(new Department("컴퓨터공학부"));
            computerMajor = majorRepository.save(new Major(null, computer));
            future = departmentRepository.save(new Department("미래융합학부"));
            entityManager.persist(AcademicProgramYear.builder()
                .studentYear("2025").department(computer).major(computerMajor).build());
            entityManager.persist(AcademicProgramYear.builder()
                .studentYear("2026").department(future).build());
        });
        student = userFixture.준호_학생(computer, computerMajor);
        token = userFixture.getToken(student.getUser());
    }

    @AfterEach
    void cleanup() {
        clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025136001", "2027136001"})
    void 학부를_비우면_연도_데이터_유무와_관계없이_거절한다(String number) throws Exception {
        update(number, null, 400);
        assertAcademic("2019136135", computer, computerMajor);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "202", "2027", "2027136", "2027abcdef", "abcd136001"})
    void 저장된_학번이_잘못되면_요청에서_학번을_생략해도_거절한다(String number) throws Exception {
        setAcademic(number, computer, computerMajor);

        update(null, computer.getName(), 400);

        assertAcademic(number, computer, computerMajor);
    }

    @Test
    void 학번을_생략하면_기존_학번으로_연도_검증한다() throws Exception {
        setAcademic("2025136001", future, null);

        update(null, future.getName(), 400);

        assertAcademic("2025136001", future, null);
    }

    @Test
    void 학번을_생략해도_기존_학번과_학부가_유효하면_저장한다() throws Exception {
        setAcademic("2025136001", computer, computerMajor);

        update(null, computer.getName(), 200);

        assertAcademic("2025136001", computer, computerMajor);
    }

    @ParameterizedTest
    @CsvSource({
        "/user/student/me, student_number", "/user/student/me, major",
        "/v2/users/students/me, student_number", "/v2/users/students/me, major"
    })
    void 회원정보_수정에서_학번이나_학부를_빼도_기존_정보를_지우지_않는다(String path, String field) throws Exception {
        Map<String, Object> request = new HashMap<>(Map.of(
            "student_number", "2025136001", "major", computer.getName(),
            "gender", 1, "name", "서정빈", "nickname", "duehee",
            "phone_number", student.getUser().getPhoneNumber()));
        request.remove(field);

        mockMvc.perform(put(path).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertAcademic("2019136135", computer, computerMajor);
    }

    @Test
    void 연도_데이터가_없어도_기존_전공이_다른_학부_소속이면_거절한다() throws Exception {
        Major other = transactionTemplate.execute(transaction ->
            majorRepository.save(new Major("다른학부전공", future)));
        setAcademic("2027136001", computer, other);

        update(null, computer.getName(), 404);

        assertAcademic("2027136001", computer, other);
    }

    @ParameterizedTest
    @ValueSource(strings = {"student_number", "major"})
    void 이메일_회원가입에서_학번이나_학부가_없으면_인증_정보를_만들지_않는다(String field) throws Exception {
        Map<String, Object> request = registration();
        request.remove(field);

        mockMvc.perform(post("/user/student/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(studentRedisRepository.findById((String) request.get("email"))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"student_number", "major"})
    void 이미_발급된_인증_정보에_학번이나_학부가_없어도_학생을_만들지_않는다(String field) throws Exception {
        Map<String, Object> request = registration();
        request.remove(field);
        RegisterStudentRequest invalid = objectMapper.convertValue(request, RegisterStudentRequest.class);
        studentRedisRepository.save(UnAuthenticatedStudentInfo.of(invalid, "academic-validation-token"));

        mockMvc.perform(get("/user/authenticate").queryParam("auth_token", "academic-validation-token"))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.findByEmail(invalid.email())).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2027ABC001"})
    void 관리자도_학번이_없거나_잘못되면_회원정보를_바꾸지_않는다(String number) throws Exception {
        adminUpdate(number, computer.getName(), 400);

        assertAcademic("2019136135", computer, computerMajor);
        assertUserUnchanged();
    }

    @Test
    void 관리자도_학번에_없는_학부로_바꿀_수_없다() throws Exception {
        setAcademic("2019136135", computer, null);

        adminUpdate("2025136001", future.getName(), 400);

        assertAcademic("2019136135", computer, null);
        assertUserUnchanged();
    }

    @Test
    void 관리자도_연도_정보가_없다고_기존_전공과_다른_학부를_저장할_수_없다() throws Exception {
        adminUpdate("2027136001", future.getName(), 404);

        assertAcademic("2019136135", computer, computerMajor);
        assertUserUnchanged();
    }

    @Test
    void 관리자도_연도_정보가_없고_학적이_유효하면_저장한다() throws Exception {
        adminUpdate("2027136001", computer.getName(), 200);

        assertAcademic("2027136001", computer, computerMajor);
    }

    private void adminUpdate(String number, String department, int expectedStatus) throws Exception {
        String adminToken = userFixture.getToken(userFixture.코인_운영자().getUser());
        Map<String, Object> request = new HashMap<>(Map.of("gender", 1, "nickname", "updated"));
        request.put("student_number", number);
        request.put("major", department);
        mockMvc.perform(put("/admin/users/student/{id}", student.getId())
                .header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(expectedStatus));
    }

    private void assertUserUnchanged() {
        transactionTemplate.executeWithoutResult(transaction -> {
            Student saved = studentRepository.getById(student.getId());
            assertThat(saved.getUser().getNickname()).isEqualTo(student.getUser().getNickname());
            assertThat(saved.getUser().getName()).isEqualTo(student.getUser().getName());
            assertThat(saved.getUser().getPhoneNumber()).isEqualTo(student.getUser().getPhoneNumber());
        });
    }

    private Map<String, Object> registration() {
        return new HashMap<>(Map.of(
            "email", "academic-validation@koreatech.ac.kr", "nickname", "newstudent",
            "password", "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
            "student_number", "2025136001", "major", computer.getName()));
    }

    private void setAcademic(String number, Department department, Major major) {
        transactionTemplate.executeWithoutResult(transaction -> studentRepository.getById(student.getId())
            .updateStudentAcademicInfo(number, department, major));
    }

    private void update(String number, String department, int expectedStatus) throws Exception {
        mockMvc.perform(put("/user/student/academic-info").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateStudentAcademicInfoRequest(number, department, null))))
            .andExpect(status().is(expectedStatus));
    }

    private void assertAcademic(String number, Department department, Major major) {
        transactionTemplate.executeWithoutResult(transaction -> {
            Student saved = studentRepository.getById(student.getId());
            assertThat(saved.getStudentNumber()).isEqualTo(number);
            assertThat(saved.getDepartment().getId()).isEqualTo(department.getId());
            if (major == null) {
                assertThat(saved.getMajor()).isNull();
            } else {
                assertThat(saved.getMajor().getId()).isEqualTo(major.getId());
            }
        });
    }
}
