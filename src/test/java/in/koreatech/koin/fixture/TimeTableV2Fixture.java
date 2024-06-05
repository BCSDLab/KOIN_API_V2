package in.koreatech.koin.fixture;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import in.koreatech.koin.domain.timetable.model.TimeTableLecture;
import in.koreatech.koin.domain.timetable.repository.TimeTableFrameRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableLectureRepository;
import in.koreatech.koin.domain.user.model.User;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class TimeTableV2Fixture {

    private final TimeTableFrameRepository timeTableFrameRepository;

    private final TimeTableLectureRepository timeTableLectureRepository;

    public TimeTableV2Fixture(
        TimeTableFrameRepository timeTableFrameRepository,
        TimeTableLectureRepository timeTableLectureRepository
    ) {
        this.timeTableFrameRepository = timeTableFrameRepository;
        this.timeTableLectureRepository = timeTableLectureRepository;
    }

    public TimeTableFrame 시간표1(User user, Semester semester) {
        return timeTableFrameRepository.save(
            TimeTableFrame.builder()
                .user(user)
                .semester(semester)
                .name("시간표1")
                .isMain(true)
                .build()
        );
    }

    public TimeTableFrame 시간표2(User user, Semester semester) {
        return timeTableFrameRepository.save(
            TimeTableFrame.builder()
                .user(user)
                .semester(semester)
                .name("시간표2")
                .isMain(false)
                .build()
        );
    }

    public TimeTableFrame 시간표3(User user, Semester semester) {
        TimeTableFrame frame = TimeTableFrame.builder()
            .user(user)
            .semester(semester)
            .name("시간표3")
            .isMain(false)
            .timeTableLectures(new ArrayList<>())
            .build();

        TimeTableLecture timeTableLecture1 = TimeTableLecture.builder()
            .className("커스텀1")
            .classTime("[932]")
            .isDeleted(false)
            .timetableFrame(frame)
            .build();

        TimeTableLecture timeTableLecture2 = TimeTableLecture.builder()
            .className("커스텀2")
            .classTime("[933]")
            .isDeleted(false)
            .timetableFrame(frame)
            .build();

        frame.getTimeTableLectures().add(timeTableLecture1);
        frame.getTimeTableLectures().add(timeTableLecture2);

        return timeTableFrameRepository.save(frame);
    }

    public TimeTableFrame 시간표4(User user, Semester semester, Lecture lecture1, Lecture lecture2) {
        TimeTableFrame frame = TimeTableFrame.builder()
            .user(user)
            .semester(semester)
            .name("시간표4")
            .isMain(false)
            .timeTableLectures(new ArrayList<>())
            .build();

        TimeTableLecture timeTableLecture1 = TimeTableLecture.builder()
            .isDeleted(false)
            .lectures(lecture1)
            .timetableFrame(frame)
            .build();

        TimeTableLecture timeTableLecture2 = TimeTableLecture.builder()
            .isDeleted(false)
            .lectures(lecture2)
            .timetableFrame(frame)
            .build();

        frame.getTimeTableLectures().add(timeTableLecture1);
        frame.getTimeTableLectures().add(timeTableLecture2);

        return timeTableFrameRepository.save(frame);
    }

    public TimeTableFrame 시간표5(User user, Semester semester, Lecture lecture1) {
        TimeTableFrame frame = TimeTableFrame.builder()
            .user(user)
            .semester(semester)
            .name("시간표4")
            .isMain(false)
            .timeTableLectures(new ArrayList<>())
            .build();

        TimeTableLecture timeTableLecture1 = TimeTableLecture.builder()
            .isDeleted(false)
            .lectures(lecture1)
            .timetableFrame(frame)
            .build();

        TimeTableLecture timeTableLecture2 = TimeTableLecture.builder()
            .className("커스텀1")
            .classTime("[933]")
            .classPlace("2공")
            .professor("김성재")
            .memo("공부 하기 싫다")
            .isDeleted(false)
            .timetableFrame(frame)
            .build();

        frame.getTimeTableLectures().add(timeTableLecture1);
        frame.getTimeTableLectures().add(timeTableLecture2);

        return timeTableFrameRepository.save(frame);
    }
}
