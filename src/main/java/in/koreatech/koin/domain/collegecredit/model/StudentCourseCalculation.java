package in.koreatech.koin.domain.collegecredit.model;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import org.hibernate.annotations.Where;

@Getter
@Entity
@Table(name = "student_course_calculation")
@Where(clause = "is_deleted=0")
@NoArgsConstructor
public class StudentCourseCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_graduation_requirements_id")
    private StandardGraduationRequirements standardGraduationRequirements;

    @Column(name = "completed_grades", nullable = false)
    private int completedGrades;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    public StudentCourseCalculation(User user, StandardGraduationRequirements standardGraduationRequirements,
        int completedGrades) {
        this.user = user;
        this.standardGraduationRequirements = standardGraduationRequirements;
        this.completedGrades = completedGrades;
    }

    public void calculateCompletedGrades(int completedGrades) {
        this.completedGrades = completedGrades;
    }
}
