package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class CourseTypeFixture {

    private final CourseTypeRepository courseTypeRepository;

    public CourseTypeFixture(CourseTypeRepository courseTypeRepository) {
        this.courseTypeRepository = courseTypeRepository;
    }

    public CourseType 전공_필수() {
        return courseTypeRepository.save(
            new CourseType("전공 필수")
        );
    }

    public CourseType HRD_필수() {
        return courseTypeRepository.save(
            new CourseType("MSC 필수")
        );
    }

    public CourseType 교양_필수() {
        return courseTypeRepository.save(
            new CourseType("교양 필수")
        );
    }
}
