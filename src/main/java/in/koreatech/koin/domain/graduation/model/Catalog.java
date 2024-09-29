package in.koreatech.koin.domain.graduation.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(
    name = "catalog",
    indexes = @Index(name = "catalog_year_index", columnList = "year")
)
@NoArgsConstructor(access = PROTECTED)
public class Catalog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Size(max = 20)
    @Column(name = "year", nullable = false, length = 20)
    private String year;

    @NotNull
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotNull
    @Size(max = 255)
    @Column(name = "lecture_name", nullable = false, length = 255)
    private String lectureName;

    @NotNull
    @Column(name = "credit", nullable = false)
    private int credit = 0;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "course_type_id", nullable = false)
    private CourseType courseType;

    @Builder
    public Catalog(
        String year, String code,
        String lectureName, Department department,
        int credit, CourseType courseType
    ) {
        this.year = year;
        this.code = code;
        this.lectureName = lectureName;
        this.department = department;
        this.credit = credit;
        this.courseType = courseType;
    }
}
