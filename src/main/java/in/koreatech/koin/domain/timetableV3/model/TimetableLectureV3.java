package in.koreatech.koin.domain.timetableV3.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.global.domain.BaseEntity;
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
public class TimetableLectureV3 extends BaseEntity {

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
    private TimetableFrameV3 timetableFrameV3;

    @Builder
    public TimetableLectureV3(String classTitle, String classTime, String classPlace, String professor,
        String grades, String memo, boolean isDeleted, Lecture lecture, TimetableFrameV3 timetableFrameV3) {
        this.classTitle = classTitle;
        this.classTime = classTime;
        this.classPlace = classPlace;
        this.professor = professor;
        this.grades = grades;
        this.memo = memo;
        this.isDeleted = isDeleted;
        this.lecture = lecture;
        this.timetableFrameV3 = timetableFrameV3;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void undelete() {
        this.isDeleted = false;
    }
}
