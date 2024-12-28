package in.koreatech.koin.domain.timetable.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.timetableV3.model.LectureInformation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "lectures")
@NoArgsConstructor(access = PROTECTED)
public class Lecture {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "semester_date", nullable = false)
    private String semester;

    @Size(max = 255)
    @NotNull
    @Column(name = "code", nullable = false)
    private String code;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "grades", nullable = false)
    private String grades;

    @Size(max = 255)
    @NotNull
    @Column(name = "class", nullable = false)
    private String lectureClass;

    @Size(max = 255)
    @Column(name = "regular_number")
    private String regularNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "department", nullable = false)
    private String department;

    @Size(max = 255)
    @NotNull
    @Column(name = "target", nullable = false)
    private String target;

    @Size(max = 255)
    @Column(name = "professor")
    private String professor;

    @Size(max = 255)
    @Column(name = "is_english")
    private String isEnglish;

    @Size(max = 255)
    @NotNull
    @Column(name = "design_score", nullable = false)
    private String designScore;

    @Size(max = 255)
    @NotNull
    @Column(name = "is_elearning", nullable = false)
    private String isElearning;

    @Size(max = 255)
    @NotNull
    @Column(name = "class_time", nullable = false)
    private String classTime;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lecture", orphanRemoval = true)
    private List<LectureInformation> lectureInformations = new ArrayList<>();

    @Builder
    public Lecture(
        String semester,
        String code,
        String name,
        String grades,
        String lectureClass,
        String regularNumber,
        String department,
        String target,
        String professor,
        String isEnglish,
        String designScore,
        String isElearning,
        String classTime
    ) {
        this.semester = semester;
        this.code = code;
        this.name = name;
        this.grades = grades;
        this.lectureClass = lectureClass;
        this.regularNumber = regularNumber;
        this.department = department;
        this.target = target;
        this.professor = professor;
        this.isEnglish = isEnglish;
        this.designScore = designScore;
        this.isElearning = isElearning;
        this.classTime = classTime;
    }
}
