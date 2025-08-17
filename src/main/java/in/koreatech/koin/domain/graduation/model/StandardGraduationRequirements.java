package in.koreatech.koin.domain.graduation.model;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "standard_graduation_requirements")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class StandardGraduationRequirements extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(max = 20)
    @Column(name = "year", nullable = false, length = 20)
    private String year;

    @NotNull
    @Column(name = "required_grades", nullable = false)
    private int requiredGrades;

    @NotNull
    @Column(name = "is_deleted", columnDefinition = "TINYINT")
    private boolean isDeleted = false;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "course_type_id")
    private CourseType courseType;

    @Builder
    private StandardGraduationRequirements(
        String year, int requiredGrades,
        boolean isDeleted, Major major,
        CourseType courseType
    ) {
        this.year = year;
        this.requiredGrades = requiredGrades;
        this.isDeleted = isDeleted;
        this.major = major;
        this.courseType = courseType;
    }
}
