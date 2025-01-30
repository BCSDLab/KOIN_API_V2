package in.koreatech.koin.domain.graduation.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import com.google.firebase.database.annotations.NotNull;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "course_type")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CourseType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull
    @Column(name = "is_deleted", columnDefinition = "TINYINT")
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "courseType")
    private List<Catalog> catalog = new ArrayList<>();

    @Builder
    private CourseType(
        String name,
        Integer id,
        Integer year,
        boolean isDeleted,
        List<Catalog> catalog
    ) {
        this.name = name;
        this.id = id;
        this.year = year;
        this.isDeleted = isDeleted;
        this.catalog = catalog;
    }
}
