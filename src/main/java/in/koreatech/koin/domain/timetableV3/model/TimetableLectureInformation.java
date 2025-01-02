package in.koreatech.koin.domain.timetableV3.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "timetable_lecture_information")
@NoArgsConstructor(access = PROTECTED)
public class TimetableLectureInformation {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "start_time")
    private Integer startTime;

    @Column(name = "end_time")
    private Integer endTime;

    @Column(name = "place", length = 255)
    private String place;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "lecture_information_id")
    private LectureInformation lectureInformation;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "timetable_lecture_id")
    private TimetableLecture timetableLecture;

    @Builder
    private TimetableLectureInformation(
        Integer id,
        Integer startTime,
        Integer endTime,
        String place,
        LectureInformation lectureInformation,
        TimetableLecture timetableLecture
    ) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.place = place;
        this.lectureInformation = lectureInformation;
        this.timetableLecture = timetableLecture;
    }
}
