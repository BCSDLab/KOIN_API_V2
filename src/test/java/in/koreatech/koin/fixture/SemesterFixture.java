package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class SemesterFixture {

    private final SemesterRepository semesterRepository;

    public SemesterFixture(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    public Semester semester(String semester) {
        return semesterRepository.save(
            Semester.builder()
                .semester(semester)
                .build()
        );
    }
}
