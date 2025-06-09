package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class SemesterFixture {

    private final SemesterRepositoryV3 semesterRepositoryV3;

    public SemesterFixture(SemesterRepositoryV3 semesterRepositoryV3) {
        this.semesterRepositoryV3 = semesterRepositoryV3;
    }

    public Semester semester_2019년도_1학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20191")
            .year(2019)
            .term(Term.FIRST)
            .build()
        );
    }

    public Semester semester_2019년도_2학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20192")
            .year(2019)
            .term(Term.SECOND)
            .build()
        );
    }

    public Semester semester_2019년도_여름() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2019-여름")
            .year(2019)
            .term(Term.SUMMER)
            .build()
        );
    }

    public Semester semester_2019년도_겨울() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2019-겨울")
            .year(2019)
            .term(Term.WINTER)
            .build()
        );
    }

    public Semester semester_2020년도_1학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20201")
            .year(2020)
            .term(Term.FIRST)
            .build()
        );
    }

    public Semester semester_2020년도_2학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20202")
            .year(2020)
            .term(Term.SECOND)
            .build()
        );
    }

    public Semester semester_2020년도_여름() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2020-여름")
            .year(2020)
            .term(Term.SUMMER)
            .build()
        );
    }

    public Semester semester_2020년도_겨울() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2020-겨울")
            .year(2020)
            .term(Term.WINTER)
            .build()
        );
    }

    public Semester semester_2021년도_1학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20211")
            .year(2021)
            .term(Term.FIRST)
            .build()
        );
    }

    public Semester semester_2021년도_2학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20212")
            .year(2021)
            .term(Term.SECOND)
            .build()
        );
    }

    public Semester semester_2021년도_여름() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2021-여름")
            .year(2021)
            .term(Term.SUMMER)
            .build()
        );
    }

    public Semester semester_2021년도_겨울() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2021-겨울")
            .year(2021)
            .term(Term.WINTER)
            .build()
        );
    }

    public Semester semester_2022년도_1학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20221")
            .year(2022)
            .term(Term.FIRST)
            .build()
        );
    }

    public Semester semester_2022년도_2학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20222")
            .year(2022)
            .term(Term.SECOND)
            .build()
        );
    }

    public Semester semester_2022년도_여름() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2022-여름")
            .year(2022)
            .term(Term.SUMMER)
            .build()
        );
    }

    public Semester semester_2022년도_겨울() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2022-겨울")
            .year(2022)
            .term(Term.WINTER)
            .build()
        );
    }

    public Semester semester_2023년도_1학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20231")
            .year(2023)
            .term(Term.FIRST)
            .build()
        );
    }

    public Semester semester_2023년도_2학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20232")
            .year(2023)
            .term(Term.SECOND)
            .build()
        );
    }

    public Semester semester_2023년도_여름() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2023-여름")
            .year(2023)
            .term(Term.SUMMER)
            .build()
        );
    }

    public Semester semester_2023년도_겨울() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2023-겨울")
            .year(2023)
            .term(Term.WINTER)
            .build()
        );
    }

    public Semester semester_2024년도_1학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20241")
            .year(2024)
            .term(Term.FIRST)
            .build()
        );
    }

    public Semester semester_2024년도_2학기() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("20242")
            .year(2024)
            .term(Term.SECOND)
            .build()
        );
    }

    public Semester semester_2024년도_여름() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2024-여름")
            .year(2024)
            .term(Term.SUMMER)
            .build()
        );
    }

    public Semester semester_2024년도_겨울() {
        return semesterRepositoryV3.save(
            Semester.builder()
            .semester("2024-겨울")
            .year(2024)
            .term(Term.WINTER)
            .build()
        );
    }
}
