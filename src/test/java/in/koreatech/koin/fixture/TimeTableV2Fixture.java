package in.koreatech.koin.fixture;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.timetable.model.TimetableLecture;
import in.koreatech.koin.domain.timetable.repository.TimetableFrameRepository;
import in.koreatech.koin.domain.timetable.repository.TimetableLectureRepository;
import in.koreatech.koin.domain.user.model.User;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class TimeTableV2Fixture {

    private final TimetableFrameRepository timetableFrameRepository;

    private final TimetableLectureRepository timetableLectureRepository;

    public TimeTableV2Fixture(
        TimetableFrameRepository timetableFrameRepository,
        TimetableLectureRepository timetableLectureRepository
    ) {
        this.timetableFrameRepository = timetableFrameRepository;
        this.timetableLectureRepository = timetableLectureRepository;
    }

    public TimetableFrame 시간표1(User user, Semester semester) {
        return timetableFrameRepository.save(
            TimetableFrame.builder()
                .user(user)
                .semester(semester)
                .name("시간표1")
                .isMain(true)
                .build()
        );
    }

    public TimetableFrame 시간표2(User user, Semester semester) {
        return timetableFrameRepository.save(
            TimetableFrame.builder()
                .user(user)
                .semester(semester)
                .name("시간표2")
                .isMain(false)
                .build()
        );
    }

    public TimetableFrame 시간표3(User user, Semester semester) {
        TimetableFrame frame = TimetableFrame.builder()
            .user(user)
            .semester(semester)
            .name("시간표3")
            .isMain(false)
            .timetableLectures(new ArrayList<>())
            .build();

        TimetableLecture timetableLecture1 = TimetableLecture.builder()
            .grades("0")
            .classTitle("커스텀1")
            .classTime("[932]")
            .isDeleted(false)
            .timetableFrame(frame)
            .build();

        TimetableLecture timetableLecture2 = TimetableLecture.builder()
            .grades("0")
            .classTitle("커스텀2")
            .classTime("[933]")
            .isDeleted(false)
            .timetableFrame(frame)
            .build();

        frame.getTimetableLectures().add(timetableLecture1);
        frame.getTimetableLectures().add(timetableLecture2);

        return timetableFrameRepository.save(frame);
    }

    public TimetableFrame 시간표4(User user, Semester semester, Lecture lecture1, Lecture lecture2) {
        TimetableFrame frame = TimetableFrame.builder()
            .user(user)
            .semester(semester)
            .name("시간표4")
            .isMain(false)
            .timetableLectures(new ArrayList<>())
            .build();

        TimetableLecture timetableLecture1 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(false)
            .lecture(lecture1)
            .timetableFrame(frame)
            .build();

        TimetableLecture timetableLecture2 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(false)
            .lecture(lecture2)
            .timetableFrame(frame)
            .build();

        frame.getTimetableLectures().add(timetableLecture1);
        frame.getTimetableLectures().add(timetableLecture2);

        return timetableFrameRepository.save(frame);
    }

    public TimetableFrame 시간표5(User user, Semester semester, Lecture lecture1) {
        TimetableFrame frame = TimetableFrame.builder()
            .user(user)
            .semester(semester)
            .name("시간표4")
            .isMain(false)
            .timetableLectures(new ArrayList<>())
            .build();

        TimetableLecture timetableLecture1 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(false)
            .lecture(lecture1)
            .timetableFrame(frame)
            .build();

        TimetableLecture timetableLecture2 = TimetableLecture.builder()
            .classTitle("커스텀1")
            .classTime("[933]")
            .classPlace("2공")
            .professor("김성재")
            .grades("0")
            .memo("공부 하기 싫다")
            .isDeleted(false)
            .timetableFrame(frame)
            .build();

        frame.getTimetableLectures().add(timetableLecture1);
        frame.getTimetableLectures().add(timetableLecture2);

        return timetableFrameRepository.save(frame);
    }

    public TimetableFrame 시간표6(User user, Semester semester, Lecture lecture1, Lecture lecture2) {
        TimetableFrame frame = TimetableFrame.builder()
            .user(user)
            .semester(semester)
            .name("시간표6")
            .isMain(true)
            .timetableLectures(new ArrayList<>())
            .build();

        TimetableLecture timetableLecture1 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(false)
            .lecture(lecture1)
            .timetableFrame(frame)
            .build();

        TimetableLecture timetableLecture2 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(false)
            .lecture(lecture2)
            .timetableFrame(frame)
            .build();

        frame.getTimetableLectures().add(timetableLecture1);
        frame.getTimetableLectures().add(timetableLecture2);

        return timetableFrameRepository.save(frame);
    }
}