package in.koreatech.koin.domain.collegecredit.model;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import org.hibernate.annotations.Where;

@Getter
@Entity
@Table(name = "course_type")
@Where(clause = "is_deleted=0")
@NoArgsConstructor
public class CourseType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "year", nullable = false)
    private String year;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    public CourseType(String year, String name) {
        this.year = year;
        this.name = name;
    }
}
