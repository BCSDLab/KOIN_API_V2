package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.repository.MajorRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class MajorAcceptanceFixture {

    private final MajorRepository majorRepository;

    public MajorAcceptanceFixture(MajorRepository majorRepository) {
        this.majorRepository = majorRepository;
    }

    public Major 기계공학전공(Department department) {
        return majorRepository.save(Major.builder()
            .name(null)
            .department(department)
            .build()
        );
    }

    public Major 컴퓨터공학전공(Department department) {
        return majorRepository.save(Major.builder()
            .name(null)
            .department(department)
            .build()
        );
    }
}
