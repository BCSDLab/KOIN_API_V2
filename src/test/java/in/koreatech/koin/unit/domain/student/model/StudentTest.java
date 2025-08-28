package in.koreatech.koin.unit.domain.student.model;

import static org.assertj.core.api.Assertions.assertThat;

import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.unit.fixture.StudentFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StudentTest {

    @Mock
    private Department department;

    @Mock
    private Major major;

    @Mock
    private Department otherDepartment;

    @Mock
    private Major otherMajor;

    private Student student;

    @BeforeEach
    void init() {
        student = StudentFixture.코인_학생(department, major);
    }

    @Nested
    class UpdateTest {

        @Test
        void 학번과_학과를_변경하면_학번과_학과만_변경된다() {
            student.updateInfo("2023100657", otherDepartment);
            assertThat(student.getStudentNumber()).isEqualTo("2023100657");
            assertThat(student.getDepartment()).isSameAs(otherDepartment);

            assertThat(student.getMajor()).isSameAs(major);
        }

        @Test
        void 학번만_변경하면_학번만_변경된다() {
            student.updateStudentNumber("2023100657");
            assertThat(student.getStudentNumber()).isEqualTo("2023100657");

            assertThat(student.getDepartment()).isSameAs(department);
            assertThat(student.getMajor()).isSameAs(major);
        }

        @Test
        void 학부와_전공을_변경하면_학부와_전공만_변경된다() {
            student.updateDepartmentMajor(otherDepartment, otherMajor);
            assertThat(student.getDepartment()).isSameAs(otherDepartment);
            assertThat(student.getMajor()).isSameAs(otherMajor);

            assertThat(student.getStudentNumber()).isEqualTo("2019136135");
        }

        @Test
        void 학번_학부_전공을_변경하면_학번_학부_전공만_변경된다() {
            student.updateStudentAcademicInfo("2023100657", otherDepartment, otherMajor);
            assertThat(student.getStudentNumber()).isEqualTo("2023100657");
            assertThat(student.getDepartment()).isSameAs(otherDepartment);
            assertThat(student.getMajor()).isSameAs(otherMajor);
        }

        @Nested
        class isNotSameStudentNumberTest {

            @Test
            void 학번이_같으면_false를_반환한다() {
                boolean result = student.isNotSameStudentNumber("2019136135");
                assertThat(result).isFalse();
            }

            @Test
            void 학번이_다르면_true를_반환한다() {
                boolean result = student.isNotSameStudentNumber("2023100657");
                assertThat(result).isTrue();
            }
        }

        @Nested
        class isNotSameDepartmentTest {

            @Test
            void 학부가_같으면_false를_반환한다() {
                boolean result = student.isNotSameDepartment(department);
                assertThat(result).isFalse();
            }

            @Test
            void 학부가_다르면_true를_반환한다() {
                boolean result = student.isNotSameDepartment(otherDepartment);
                assertThat(result).isTrue();
            }
        }
    }
}
