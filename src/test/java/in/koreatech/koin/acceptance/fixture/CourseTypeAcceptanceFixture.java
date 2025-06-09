package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class CourseTypeAcceptanceFixture {

    private final CourseTypeRepository courseTypeRepository;

    public CourseTypeAcceptanceFixture(CourseTypeRepository courseTypeRepository) {
        this.courseTypeRepository = courseTypeRepository;
    }

    public CourseType 전공_필수() {
        return courseTypeRepository.save(
            CourseType.builder()
                .name("전공 필수")
                .build()
        );
    }

    public CourseType HRD_필수() {
        return courseTypeRepository.save(
            CourseType.builder()
                .name("HRD 필수")
                .build()
        );
    }

    public CourseType 교양_필수() {
        return courseTypeRepository.save(
            CourseType.builder()
                .name("교양 필수")
                .build()
        );
    }

    public CourseType 이수_구분_선택() {
        return courseTypeRepository.save(
            CourseType.builder()
                .name("이수구분선택")
                .build()
        );
    }
}
