package in.koreatech.koin.domain.collegecredit.model;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import org.hibernate.annotations.Where;

@Getter
@Entity
@Table(name = "standard_graduation_requirements")
@Where(clause = "is_deleted=0")
@NoArgsConstructor
public class StandardGraduationRequirements extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "year", nullable = false)
    private String year;

    @Column(name = "department", nullable = false)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_type_id")
    private CourseType courseType;

    @Column(name = "required_grades", nullable = false)
    private int requiredGrades;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder
    public StandardGraduationRequirements(String year, String department, CourseType courseType,
        int requiredGrades) {
        this.year = year;
        this.department = department;
        this.courseType = courseType;
        this.requiredGrades = requiredGrades;
    }
}
