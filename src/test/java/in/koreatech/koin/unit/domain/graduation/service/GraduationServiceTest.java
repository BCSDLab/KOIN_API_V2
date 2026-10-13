package in.koreatech.koin.unit.domain.graduation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.model.DetectGraduationCalculation;
import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import in.koreatech.koin.domain.graduation.repository.DetectGraduationCalculationRepository;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.repository.StudentCourseCalculationRepository;
import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.unit.fixture.StudentFixture;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class GraduationServiceTest {

    @InjectMocks
    private GraduationService graduationService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private StudentCourseCalculationRepository studentCourseCalculationRepository;
    @Mock
    private StandardGraduationRequirementsRepository standardGraduationRequirementsRepository;
    @Mock
    private DetectGraduationCalculationRepository detectGraduationCalculationRepository;

    private final Department department = new Department("컴퓨터공학부");
    private final Major major = new Major(null, department);
    private Student student;
    private StudentCourseCalculation previousCalculation;

    @BeforeEach
    void init() {
        student = StudentFixture.준호_학생(department, major);
        ReflectionTestUtils.setField(student.getUser(), "id", 1);
        previousCalculation = StudentCourseCalculation.builder()
            .user(student.getUser())
            .standardGraduationRequirements(requirement("2024", major, 10, "전공필수", 42))
            .completedGrades(12)
            .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025136001", "2026136001"})
    void 새_졸업_기준이_없으면_기존_계산_자료를_보존한다(String studentNumber) {
        student.updateStudentNumber(studentNumber);
        when(studentCourseCalculationRepository.findAllByUserId(1)).thenReturn(List.of(previousCalculation));

        graduationService.resetStudentCourseCalculation(student, major);

        assertThat(previousCalculation.getCompletedGrades()).isEqualTo(12);
        assertThat(previousCalculation.getStandardGraduationRequirements().getYear()).isEqualTo("2024");
        verify(studentCourseCalculationRepository).findAllByUserId(1);
        verifyNoMoreInteractions(studentCourseCalculationRepository);
        verifyNoInteractions(entityManager, detectGraduationCalculationRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024136001", "2025136001"})
    void 새_졸업_기준이_있으면_기존처럼_계산을_갱신한다(String studentNumber) {
        student.updateStudentNumber(studentNumber);
        String year = studentNumber.substring(0, 4);
        Major newMajor = new Major("전자공학전공", new Department("전기전자통신공학부"));
        StandardGraduationRequirements required = requirement(year, newMajor, 10, "전공필수", 36);
        StandardGraduationRequirements elective = requirement(year, newMajor, 8, "전공선택", 34);
        DetectGraduationCalculation detection = DetectGraduationCalculation.builder()
            .user(student.getUser()).isChanged(false).build();
        when(studentCourseCalculationRepository.findAllByUserId(1)).thenReturn(List.of(previousCalculation));
        when(standardGraduationRequirementsRepository.findAllByMajorAndYear(newMajor, year))
            .thenReturn(List.of(required, elective));
        when(detectGraduationCalculationRepository.findByUserId(1)).thenReturn(Optional.of(detection));

        graduationService.resetStudentCourseCalculation(student, newMajor);

        var captor = ArgumentCaptor.forClass(StudentCourseCalculation.class);
        var order = inOrder(standardGraduationRequirementsRepository, studentCourseCalculationRepository, entityManager);
        order.verify(standardGraduationRequirementsRepository).findAllByMajorAndYear(newMajor, year);
        order.verify(studentCourseCalculationRepository).deleteAllByUserId(1);
        order.verify(entityManager).flush();
        order.verify(entityManager).clear();
        order.verify(studentCourseCalculationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(StudentCourseCalculation::getStandardGraduationRequirements)
            .containsExactly(required, elective);
        assertThat(captor.getAllValues()).allSatisfy(calculation -> {
            assertThat(calculation.getUser()).isSameAs(student.getUser());
            assertThat(calculation.getCompletedGrades()).isZero();
        });
        assertThat(detection.isChanged()).isTrue();
        verify(standardGraduationRequirementsRepository).findAllByMajorAndYear(newMajor, year);
    }

    @Test
    void 졸업_계산을_사용한_적이_없으면_자료를_새로_만들지_않는다() {
        graduationService.resetStudentCourseCalculation(student, major);

        verify(studentCourseCalculationRepository).findAllByUserId(1);
        verifyNoInteractions(standardGraduationRequirementsRepository, entityManager,
            detectGraduationCalculationRepository);
    }

    @Test
    void 학번이_없으면_기존_계산_자료를_보존한다() {
        student.updateStudentNumber(null);

        graduationService.resetStudentCourseCalculation(student, major);

        verifyNoInteractions(studentCourseCalculationRepository, standardGraduationRequirementsRepository,
            entityManager, detectGraduationCalculationRepository);
    }

    @Test
    void 전공이_없으면_기존_계산_자료를_보존한다() {
        graduationService.resetStudentCourseCalculation(student, null);

        verifyNoInteractions(studentCourseCalculationRepository, standardGraduationRequirementsRepository,
            entityManager, detectGraduationCalculationRepository);
    }

    private StandardGraduationRequirements requirement(String year, Major major, int typeId, String name, int credits) {
        return StandardGraduationRequirements.builder()
            .year(year).major(major).requiredGrades(credits)
            .courseType(CourseType.builder().id(typeId).name(name).build())
            .build();
    }
}
