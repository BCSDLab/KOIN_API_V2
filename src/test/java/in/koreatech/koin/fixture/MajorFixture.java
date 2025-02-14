package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.repository.MajorRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class MajorFixture {

    private final MajorRepository majorRepository;

    public MajorFixture(MajorRepository majorRepository) {
        this.majorRepository = majorRepository;
    }

    public void 기계공학전공() {
        majorRepository.save(Major.builder()
            .name("기계공학부")
            .build()
        );
    }

    public void 컴퓨터공학전공() {
        majorRepository.save(Major.builder()
            .name("컴퓨터공학전공")
            .build()
        );
    }
}
