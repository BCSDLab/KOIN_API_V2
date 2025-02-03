package in.koreatech.koin.domain.graduation.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "general_education_area_course_type_map")
@NoArgsConstructor(access = PROTECTED)
public class GeneralEducationAreaCourseTypeMap {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "general_education_area_id", referencedColumnName = "id", nullable = false)
    private GeneralEducationArea generalEducationArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_type_id", referencedColumnName = "id", nullable = false)
    private CourseType courseType;

    @Builder
    private GeneralEducationAreaCourseTypeMap(
        Integer id,
        GeneralEducationArea generalEducationArea,
        CourseType courseType
    ) {
        this.id = id;
        this.generalEducationArea = generalEducationArea;
        this.courseType = courseType;
    }
}
