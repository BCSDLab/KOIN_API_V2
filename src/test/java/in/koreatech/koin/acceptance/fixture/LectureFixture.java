package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class LectureFixture {

    private final LectureRepository lectureRepository;

    public LectureFixture(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    public Lecture 건축구조의_이해_및_실습(String semester) {
        return lectureRepository.save(
            Lecture.builder()
                .code("ARB244")
                .semester(semester)
                .name("건축구조의 이해 및 실습")
                .grades("3")
                .lectureClass("01")
                .regularNumber("25")
                .department("디자인ㆍ건축공학부")
                .target("디자 1 건축")
                .professor("황현식")
                .isEnglish("N")
                .designScore("0")
                .isElearning("N")
                .classTime("[200, 201, 202, 203, 204, 205, 206, 207]")
                .build()
        );
    }

    public Lecture HRD_개론(String semester) {
        return lectureRepository.save(
            Lecture.builder()
                .code("BSM590")
                .semester(semester)
                .name("컴퓨팅사고")
                .grades("3")
                .lectureClass("06")
                .regularNumber("22")
                .department("기계공학부")
                .target("기공1")
                .professor("박한수,최준호")
                .isEnglish("")
                .designScore("0")
                .isElearning("")
                .classTime("[12, 13, 14, 15, 210, 211, 212, 213]")
                .build()
        );
    }

    public Lecture 재료역학(String semester) {
        return lectureRepository.save(
            Lecture.builder()
                .code("MEB311")
                .semester(semester)
                .name("재료역학")
                .grades("3")
                .lectureClass("01")
                .regularNumber("35")
                .department("기계공학부")
                .target("기공전체")
                .professor("허준기")
                .isEnglish("")
                .designScore("0")
                .isElearning("")
                .classTime("[100, 101, 102, 103, 308, 309]")
                .build()
        );
    }

    public Lecture 영어청해(String semester) {
        return lectureRepository.save(
            Lecture.builder()
                .code("LAN324")
                .semester(semester)
                .name("영어청해")
                .grades("1")
                .lectureClass("09")
                .regularNumber("40")
                .department("교양학부")
                .target("정통2")
                .professor("김원경")
                .isEnglish("")
                .designScore("0")
                .isElearning("")
                .classTime("[200, 201, 202, 203]")
                .build()
        );
    }
}
