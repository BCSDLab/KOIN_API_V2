package in.koreatech.koin.domain.timetable.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.timetable.dto.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    @Column(name = "class_name", length = 100)
    private String className;

    @Size(max = 100)
    @Column(name = "class_time", length = 100)
    private String classTime;

    @Size(max = 30)
    @Column(name = "class_place", length = 30)
    private String classPlace;

    @Size(max = 30)
    @Column(name = "professor", length = 30)
    private String professor;

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
    @JoinColumn(name = "timetable_id")
    private TimetableFrame timetableFrame;

    @Builder
    private TimetableLecture(String className, String classTime, String classPlace, String professor,
        String memo, boolean isDeleted, Lecture lectures, TimetableFrame timetableFrame) {
        this.className = className;
        this.classTime = classTime;
        this.classPlace = classPlace;
        this.professor = professor;
        this.memo = memo;
        this.isDeleted = isDeleted;
        this.lecture = lectures;
        this.timetableFrame = timetableFrame;
    }

    public void update(TimetableLectureUpdateRequest.InnerTimetableLectureRequest request) {
        this.className = request.classTitle();
        this.classTime = request.classTime().toString();
        this.classPlace = request.classPlace();
        this.professor = request.professor();
        this.memo = request.memo();
    }

    public void update(TimetableUpdateRequest.InnerTimetableRequest request) {
        this.className = request.classTitle();
        this.classTime = request.classTime().toString();
        this.classPlace = request.classPlace();
        this.professor = request.professor();
        this.memo = request.memo();
    }
}
