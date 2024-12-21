package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetableV3.model.SemesterV3;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class SemesterFixture {

    private final SemesterRepository semesterRepository;
    private final SemesterRepositoryV3 semesterRepositoryV3;

    public SemesterFixture(SemesterRepository semesterRepository, SemesterRepositoryV3 semesterRepositoryV3) {
        this.semesterRepository = semesterRepository;
        this.semesterRepositoryV3 = semesterRepositoryV3;
    }

    public Semester semester(String semester) {
        return semesterRepository.save(
            Semester.builder()
                .semester(semester)
                .build()
        );
    }

    public SemesterV3 semesterV3_2019년도_1학기() {
        return semesterRepositoryV3.save(
            SemesterV3.builder()
            .semester("20191")
            .year(2019)
            .term(Term.FIRST)
            .build()
        );
    }

    public SemesterV3 semesterV3_2019년도_2학기() {
        return semesterRepositoryV3.save(
            SemesterV3.builder()
            .semester("20192")
            .year(2019)
            .term(Term.SECOND)
            .build()
        );
    }

    public SemesterV3 semesterV3_2019년도_여름() {
        return semesterRepositoryV3.save(
            SemesterV3.builder()
            .semester("2019-여름")
            .year(2019)
            .term(Term.SUMMER)
            .build()
        );
    }

    public SemesterV3 semesterV3_2019년도_겨울() {
        return semesterRepositoryV3.save(
            SemesterV3.builder()
            .semester("2019-겨울")
            .year(2019)
            .term(Term.WINTER)
            .build()
        );
    }

    public SemesterV3 semesterV3_2020년도_1학기() {
        return semesterRepositoryV3.save(
            SemesterV3.builder()
            .semester("20201")
            .year(2020)
            .term(Term.FIRST)
            .build()
        );
    }

    public SemesterV3 semesterV3_2020년도_2학기() {
        return semesterRepositoryV3.save(
            SemesterV3.builder()
            .semester("20202")
            .year(2020)
            .term(Term.SECOND)
            .build()
        );
    }

    public SemesterV3 semesterV3_2020년도_여름() {
        return semesterRepositoryV3.save(
            SemesterV3.builder()
            .semester("2020-여름")
            .year(2020)
            .term(Term.SUMMER)
            .build()
        );
    }

    public SemesterV3 semesterV3_2020년도_겨울() {
        return semesterRepositoryV3.save(
            SemesterV3.builder()
            .semester("2020-겨울")
            .year(2020)
            .term(Term.WINTER)
            .build()
        );
    }
}
