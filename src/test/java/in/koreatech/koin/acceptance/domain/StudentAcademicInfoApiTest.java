package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.MajorAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.model.DetectGraduationCalculation;
import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import in.koreatech.koin.domain.graduation.repository.DetectGraduationCalculationRepository;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.repository.StudentCourseCalculationRepository;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoRequest;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoResponse;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StudentAcademicInfoApiTest extends AcceptanceTest {

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;
    @Autowired
    private MajorAcceptanceFixture majorFixture;
    @Autowired
    private UserAcceptanceFixture userFixture;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StandardGraduationRequirementsRepository requirementsRepository;
    @Autowired
    private StudentCourseCalculationRepository calculationRepository;
    @Autowired
    private DetectGraduationCalculationRepository detectionRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private Department department;
    private Major major;
    private Student student;
    private String token;

    @BeforeEach
    void setup() {
        clear();
        department = departmentFixture.컴퓨터공학부();
        major = majorFixture.컴퓨터공학전공(department);
        student = userFixture.준호_학생(department, major);
        token = userFixture.getToken(student.getUser());
    }

    @AfterEach
    void cleanup() {
        clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025136001", "2026136001"})
    void 졸업_기준이_없어도_학적을_DB에_저장한다(String studentNumber) throws Exception {
        update(studentNumber, department.getName(), null)
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(
                new UpdateStudentAcademicInfoResponse(studentNumber, department.getName(), null))));

        assertAcademicInfo(studentNumber);
        transactionTemplate.executeWithoutResult(transaction -> {
            assertThat(requirementsRepository.existsByMajorIdAndYear(major.getId(), studentNumber.substring(0, 4)))
                .isFalse();
            assertThat(calculationRepository.findAllByUserId(student.getUser().getId())).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025136001", "2026136001"})
    void 새_졸업_기준이_없으면_학적을_저장하고_기존_계산_자료를_보존한다(String studentNumber) throws Exception {
        StudentCourseCalculation previous = createPreviousCalculation();

        update(studentNumber, department.getName(), null).andExpect(status().isOk());

        assertAcademicInfo(studentNumber);
        transactionTemplate.executeWithoutResult(transaction -> {
            List<StudentCourseCalculation> calculations = calculationRepository.findAllByUserId(
                student.getUser().getId());
            assertThat(calculations).hasSize(1);
            StudentCourseCalculation preserved = calculations.get(0);
            assertThat(preserved.getId()).isEqualTo(previous.getId());
            assertThat(preserved.getCompletedGrades()).isEqualTo(12);
            assertThat(preserved.getStandardGraduationRequirements().getId())
                .isEqualTo(previous.getStandardGraduationRequirements().getId());
            assertThat(preserved.getStandardGraduationRequirements().getYear()).isEqualTo("2019");
            assertThat(detectionRepository.findByUserId(student.getUser().getId()).orElseThrow().isChanged())
                .isFalse();
        });
    }

    @Test
    void 새_졸업_기준이_있으면_학적과_계산_갱신을_함께_커밋한다() throws Exception {
        StudentCourseCalculation previous = createPreviousCalculation();
        StandardGraduationRequirements newRequirement = transactionTemplate.execute(transaction -> {
            CourseType courseType = entityManager.find(CourseType.class,
                previous.getStandardGraduationRequirements().getCourseType().getId());
            StandardGraduationRequirements requirement = StandardGraduationRequirements.builder()
                .year("2024").major(major).courseType(courseType).requiredGrades(42).build();
            entityManager.persist(requirement);
            return requirement;
        });

        update("2024136001", department.getName(), null).andExpect(status().isOk());

        assertAcademicInfo("2024136001");
        transactionTemplate.executeWithoutResult(transaction -> {
            List<StudentCourseCalculation> calculations = calculationRepository.findAllByUserId(
                student.getUser().getId());
            assertThat(calculations).hasSize(1);
            StudentCourseCalculation updated = calculations.get(0);
            assertThat(updated.getId()).isNotEqualTo(previous.getId());
            assertThat(updated.getCompletedGrades()).isZero();
            assertThat(updated.getStandardGraduationRequirements().getId()).isEqualTo(newRequirement.getId());
            assertThat(detectionRepository.findByUserId(student.getUser().getId()).orElseThrow().isChanged())
                .isTrue();
        });
    }

    @Test
    void 존재하지_않는_학부는_거절하고_기존_학적을_유지한다() throws Exception {
        update("2025136001", "존재하지않는학부", null).andExpect(status().isBadRequest());

        assertAcademicInfo("2019136135");
    }

    @Test
    void 다른_학부에_속한_전공은_거절하고_기존_학적을_유지한다() throws Exception {
        Department otherDepartment = departmentFixture.기계공학부();
        transactionTemplate.executeWithoutResult(transaction ->
            entityManager.persist(Major.builder().name("다른전공").department(otherDepartment).build()));

        update("2025136001", department.getName(), "다른전공").andExpect(status().isNotFound());

        assertAcademicInfo("2019136135");
    }

    @Test
    void 학번이_10자리_숫자가_아니면_거절하고_기존_학적을_유지한다() throws Exception {
        update("202513600", department.getName(), null).andExpect(status().isBadRequest());

        assertAcademicInfo("2019136135");
    }

    private ResultActions update(String studentNumber, String departmentName, String majorName) throws Exception {
        UpdateStudentAcademicInfoRequest request = new UpdateStudentAcademicInfoRequest(
            studentNumber, departmentName, majorName);
        return mockMvc.perform(put("/user/student/academic-info")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }

    private void assertAcademicInfo(String studentNumber) {
        transactionTemplate.executeWithoutResult(transaction -> {
            Student saved = studentRepository.getById(student.getId());
            assertThat(saved.getStudentNumber()).isEqualTo(studentNumber);
            assertThat(saved.getDepartment().getId()).isEqualTo(department.getId());
            assertThat(saved.getMajor().getId()).isEqualTo(major.getId());
        });
    }

    private StudentCourseCalculation createPreviousCalculation() {
        return transactionTemplate.execute(transaction -> {
            CourseType courseType = CourseType.builder().name("전공필수").build();
            entityManager.persist(courseType);
            StandardGraduationRequirements requirement = StandardGraduationRequirements.builder()
                .year("2019").major(major).courseType(courseType).requiredGrades(42).build();
            entityManager.persist(requirement);
            StudentCourseCalculation calculation = StudentCourseCalculation.builder()
                .user(student.getUser()).standardGraduationRequirements(requirement).completedGrades(12).build();
            entityManager.persist(calculation);
            entityManager.persist(DetectGraduationCalculation.builder()
                .user(student.getUser()).isChanged(false).build());
            return calculation;
        });
    }
}
