package in.koreatech.koin.domain.timetableV3.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import jakarta.persistence.Column;
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

@Entity
@Getter
@Table(name = "timetable_regular_lecture_information")
@NoArgsConstructor(access = PROTECTED)
public class TimetableRegularLectureInformation {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "place")
    private String place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_lecture_id")
    private TimetableLecture timetableLecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_information_id")
    private LectureInformation lectureInformation;

    @Builder
    public TimetableRegularLectureInformation(
        String place,
        TimetableLecture timetableLecture,
        LectureInformation lectureInformation
    ) {
        this.place = place;
        this.timetableLecture = timetableLecture;
        this.lectureInformation = lectureInformation;
    }

    public void setTimetableLectureId(TimetableLecture timetableLecture) {
        this.timetableLecture = timetableLecture;
    }

    public void setLectureInformationId(LectureInformation lectureInformation) {
        this.lectureInformation = lectureInformation;
    }
}
