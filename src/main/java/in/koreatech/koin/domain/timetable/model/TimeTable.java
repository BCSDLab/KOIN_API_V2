package in.koreatech.koin.domain.timetable.model;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "timetables")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeTable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Size(max = 10)
    @Column(name = "code", length = 10)
    private String code;

    @NotNull
    @Size(max = 50)
    @Column(name = "class_title", nullable = false, length = 50)
    private String classTitle;

    @NotNull
    @Size(max = 100)
    @Column(name = "class_time", nullable = false, length = 100)
    private String classTime;

    @Size(max = 30)
    @Column(name = "class_place", length = 30)
    private String classPlace;

    @Size(max = 30)
    @Column(name = "professor", length = 30)
    private String professor;

    @NotNull
    @Size(max = 2)
    @Column(name = "grades", nullable = false, length = 2)
    private String grades;

    @Size(max = 3)
    @Column(name = "lecture_class", length = 3)
    private String lectureClass;

    @Size(max = 200)
    @Column(name = "target", length = 200)
    private String target;

    @Size(max = 4)
    @Column(name = "regular_number", length = 4)
    private String regularNumber;

    @Size(max = 4)
    @Column(name = "design_score", length = 4)
    private String designScore;

    @Size(max = 30)
    @Column(name = "department", length = 30)
    private String department;

    @Size(max = 200)
    @Column(name = "memo", length = 200)
    private String memo;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private TimeTable(User user, Semester semester, String code, String classTitle, String classTime,
        String classPlace, String professor, String grades, String lectureClass, String target, String regularNumber,
        String designScore, String department, String memo, Boolean isDeleted) {
        this.user = user;
        this.semester = semester;
        this.code = code;
        this.classTitle = classTitle;
        this.classTime = classTime;
        this.classPlace = classPlace;
        this.professor = professor;
        this.grades = grades;
        this.lectureClass = lectureClass;
        this.target = target;
        this.regularNumber = regularNumber;
        this.designScore = designScore;
        this.department = department;
        this.memo = memo;
        this.isDeleted = isDeleted;
    }
}
