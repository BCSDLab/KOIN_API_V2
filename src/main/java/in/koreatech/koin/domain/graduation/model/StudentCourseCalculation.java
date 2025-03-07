package in.koreatech.koin.domain.graduation.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "student_course_calculation")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class StudentCourseCalculation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "completed_grades", nullable = false)
    private int completedGrades = 0;

    @NotNull
    @Column(name = "is_deleted", columnDefinition = "TINYINT")
    private boolean isDeleted = false;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "standard_graduation_requirements_id")
    private StandardGraduationRequirements standardGraduationRequirements;

    @Builder
    private StudentCourseCalculation(
        int completedGrades, boolean isDeleted, User user,
        StandardGraduationRequirements standardGraduationRequirements
    ) {
        this.completedGrades = completedGrades;
        this.isDeleted = isDeleted;
        this.user = user;
        this.standardGraduationRequirements = standardGraduationRequirements;
    }

    public void updateCompletedGrades(int newCompletedGrades) {
        this.completedGrades = newCompletedGrades;
    }
}
