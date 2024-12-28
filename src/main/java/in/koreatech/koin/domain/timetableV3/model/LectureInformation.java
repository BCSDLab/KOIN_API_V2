package in.koreatech.koin.domain.timetableV3.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.timetable.model.Lecture;
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
@Table(name = "lecture_information")
@NoArgsConstructor(access = PROTECTED)
public class LectureInformation {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    // TODO. 시작시간과 끝시간 유효성 체크 로직 추가
    @Column(name = "start_time")
    private Integer starTime;

    @Column(name = "end_time")
    private Integer endTime;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Builder
    public LectureInformation(Integer starTime, Integer endTime, Lecture lecture) {
        this.starTime = starTime;
        this.endTime = endTime;
        this.lecture = lecture;
    }
}
