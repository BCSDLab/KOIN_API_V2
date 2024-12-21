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
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "timetable_custom_lecture_information")
@NoArgsConstructor(access = PROTECTED)
public class TimetableCustomLectureInformation {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private Integer startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private Integer endTime;

    @Column(name = "place")
    private String place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_lecture_id")
    private TimetableLecture timetableLecture;

    @Builder
    public TimetableCustomLectureInformation(
        Integer startTime,
        Integer endTime,
        String place,
        TimetableLecture timetableLecture
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.place = place;
        this.timetableLecture = timetableLecture;
    }

    public void setTimetableLectureId(TimetableLecture timetableLecture) {
        this.timetableLecture = timetableLecture;
    }
}
