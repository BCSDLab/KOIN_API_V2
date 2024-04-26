package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
import in.koreatech.koin.domain.user.model.User;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class TimeTableFixture {

    private final TimeTableRepository timeTableRepository;

    public TimeTableFixture(TimeTableRepository timeTableRepository) {
        this.timeTableRepository = timeTableRepository;
    }

    public TimeTable 컴퓨터구조(User user, Semester semester) {
        return timeTableRepository.save(
            TimeTable.builder()
                .user(user)
                .semester(semester)
                .code("CS101")
                .classTitle("컴퓨터 구조")
                .classTime("[14, 15, 16, 17, 204, 205, 206, 207]")
                .classPlace(null)
                .professor("김성재")
                .grades("3")
                .lectureClass("02")
                .target("컴부전체")
                .regularNumber("28")
                .designScore("0")
                .department("컴퓨터공학부")
                .memo(null)
                .isDeleted(false)
                .build()
        );
    }

    public TimeTable 운영체제(User user, Semester semester) {
        return timeTableRepository.save(
            TimeTable.builder()
                .user(user)
                .semester(semester)
                .code("CS102")
                .classTitle("운영체제")
                .classTime("[932]")
                .classPlace(null)
                .professor("김원경")
                .grades("3")
                .lectureClass("01")
                .target("컴부전체")
                .regularNumber("40")
                .designScore("0")
                .department("컴퓨터공학부")
                .memo(null)
                .isDeleted(false)
                .build()
        );
    }

    public TimeTable 이산수학(User user, Semester semester) {
        return timeTableRepository.save(
            TimeTable.builder()
                .user(user)
                .semester(semester)
                .code("CSE125")
                .classTitle("이산수학")
                .classTime("[14, 15, 16, 17, 312, 313]")
                .classPlace(null)
                .professor("서정빈")
                .grades("3")
                .lectureClass("01")
                .target("컴부전체")
                .regularNumber("40")
                .designScore("0")
                .department("컴퓨터공학부")
                .memo(null)
                .isDeleted(false)
                .build()
        );
    }

    public TimeTable 알고리즘및실습(User user, Semester semester) {
        return timeTableRepository.save(
            TimeTable.builder()
                .user(user)
                .semester(semester)
                .code("CSE130")
                .classTitle("알고리즘및실습")
                .classTime("[14, 15, 16, 17, 310, 311, 312, 313]")
                .classPlace(null)
                .professor("박다희")
                .grades("3")
                .lectureClass("03")
                .target("컴부전체")
                .regularNumber("32")
                .designScore("0")
                .department("컴퓨터공학부")
                .memo(null)
                .isDeleted(false)
                .build()
        );
    }
}
