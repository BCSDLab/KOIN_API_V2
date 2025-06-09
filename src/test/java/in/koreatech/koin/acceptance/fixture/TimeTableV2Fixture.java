package in.koreatech.koin.acceptance.fixture;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class TimeTableV2Fixture {

    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;

    public TimeTableV2Fixture(
        TimetableFrameRepositoryV2 timetableFrameRepositoryV2,
        TimetableLectureRepositoryV2 timetableLectureRepositoryV2
    ) {
        this.timetableFrameRepositoryV2 = timetableFrameRepositoryV2;
        this.timetableLectureRepositoryV2 = timetableLectureRepositoryV2;
    }

    public TimetableFrame 시간표1(User user, Semester semester) {
        return timetableFrameRepositoryV2.save(
            TimetableFrame.builder()
                .user(user)
                .semester(semester)
                .name("시간표1")
                .isMain(true)
                .build()
        );
    }

    public TimetableFrame 시간표2(User user, Semester semester) {
        return timetableFrameRepositoryV2.save(
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

        return timetableFrameRepositoryV2.save(frame);
    }

    public TimetableFrame 시간표4(User user, Semester semester, Lecture lecture1, Lecture lecture2, CourseType courseType1,
        CourseType courseType2) {
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
            .courseType(courseType1)
            .build();

        TimetableLecture timetableLecture2 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(false)
            .lecture(lecture2)
            .timetableFrame(frame)
            .courseType(courseType2)
            .build();

        frame.getTimetableLectures().add(timetableLecture1);
        frame.getTimetableLectures().add(timetableLecture2);

        return timetableFrameRepositoryV2.save(frame);
    }

    public TimetableFrame 시간표5(User user, Semester semester, Lecture lecture1, CourseType courseType1) {
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
            .courseType(courseType1)
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

        return timetableFrameRepositoryV2.save(frame);
    }

    public TimetableFrame 시간표6(User user, Semester semester, Lecture lecture1, Lecture lecture2, CourseType courseType1,
        CourseType courseType2) {
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
            .courseType(courseType1)
            .timetableFrame(frame)
            .build();

        TimetableLecture timetableLecture2 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(false)
            .lecture(lecture2)
            .courseType(courseType2)
            .timetableFrame(frame)
            .build();

        frame.getTimetableLectures().add(timetableLecture1);
        frame.getTimetableLectures().add(timetableLecture2);

        return timetableFrameRepositoryV2.save(frame);
    }

    public TimetableFrame 시간표7(User user, Semester semester, Lecture lecture1, Lecture lecture2, CourseType courseType1,
        CourseType courseType2) {
        TimetableFrame frame = TimetableFrame.builder()
            .user(user)
            .isDeleted(true)
            .semester(semester)
            .name("시간표7")
            .isMain(true)
            .timetableLectures(new ArrayList<>())
            .build();

        TimetableLecture timetableLecture1 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(true)
            .lecture(lecture1)
            .courseType(courseType1)
            .timetableFrame(frame)
            .build();

        TimetableLecture timetableLecture2 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(true)
            .lecture(lecture2)
            .courseType(courseType2)
            .timetableFrame(frame)
            .build();

        frame.getTimetableLectures().add(timetableLecture1);
        frame.getTimetableLectures().add(timetableLecture2);

        return timetableFrameRepositoryV2.save(frame);
    }

    public TimetableFrame 시간표8(User user, Semester semester, Lecture lecture1, Lecture lecture2, CourseType courseType1,
        CourseType courseType2) {
        TimetableFrame frame = TimetableFrame.builder()
            .user(user)
            .isDeleted(false)
            .semester(semester)
            .name("시간표7")
            .isMain(true)
            .timetableLectures(new ArrayList<>())
            .build();

        TimetableLecture timetableLecture1 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(true)
            .lecture(lecture1)
            .courseType(courseType1)
            .timetableFrame(frame)
            .build();

        TimetableLecture timetableLecture2 = TimetableLecture.builder()
            .grades("0")
            .isDeleted(true)
            .lecture(lecture2)
            .courseType(courseType2)
            .timetableFrame(frame)
            .build();

        frame.getTimetableLectures().add(timetableLecture1);
        frame.getTimetableLectures().add(timetableLecture2);

        return timetableFrameRepositoryV2.save(frame);
    }
}
