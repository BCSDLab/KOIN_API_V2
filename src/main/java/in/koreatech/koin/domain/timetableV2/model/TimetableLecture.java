package in.koreatech.koin.domain.timetableV2.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.Optional;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.model.GeneralEducationArea;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "timetable_lecture")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class TimetableLecture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 100)
    @Column(name = "class_title", length = 100)
    private String classTitle;

    @Lob
    @Column(name = "class_time", columnDefinition = "TEXT")
    private String classTime;

    @Lob
    @Column(name = "class_place", columnDefinition = "TEXT")
    private String classPlace;

    @Size(max = 30)
    @Column(name = "professor", length = 30)
    private String professor;

    @Size(max = 2)
    @NotNull
    @Column(name = "grades", nullable = false)
    private String grades = "0";

    @Size(max = 200)
    @Column(name = "memo", length = 200)
    private String memo;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lectures_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frame_id")
    private TimetableFrame timetableFrame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_type_id")
    private CourseType courseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "general_education_area_id")
    private GeneralEducationArea generalEducationArea;

    @Builder
    public TimetableLecture(String classTitle, String classTime, String classPlace, String professor,
        String grades, String memo, boolean isDeleted, Lecture lecture, TimetableFrame timetableFrame,
        CourseType courseType, GeneralEducationArea generalEducationArea) {
        this.classTitle = classTitle;
        this.classTime = classTime;
        this.classPlace = classPlace;
        this.professor = professor;
        this.grades = grades;
        this.memo = memo;
        this.isDeleted = isDeleted;
        this.lecture = lecture;
        this.timetableFrame = timetableFrame;
        this.courseType = courseType;
        this.generalEducationArea = generalEducationArea;
    }

    public void update(
        String classTitle,
        String classTime,
        String classPlace,
        String professor,
        String grades,
        String memo
    ) {
        this.classTitle = classTitle;
        this.classTime = classTime;
        this.classPlace = classPlace;
        this.professor = professor;
        this.grades = grades;
        this.memo = memo;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void undelete() {
        this.isDeleted = false;
    }

    public void updateCustomLecture(String classTitle, String professor, String classTime, String classPlace) {
        this.classTitle = classTitle;
        this.professor = professor;
        this.classTime = classTime;
        this.classPlace = classPlace;
    }

    public void updateRegularLecture(String classTitle, String classPlace, CourseType courseType,
        GeneralEducationArea generalEducationArea) {
        String newClassTitle = Optional.ofNullable(lecture)
            .map(Lecture::getName)
            .orElse(this.classTitle);

        if (!newClassTitle.equals(classTitle)) {
            this.classTitle = classTitle;
        }
        this.classPlace = classPlace;
        this.courseType = courseType;
        this.generalEducationArea = generalEducationArea;
    }
}
