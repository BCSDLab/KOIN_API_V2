package in.koreatech.koin.unit.fixture;

import in.koreatech.koin.domain.club.model.ClubCategory;
import org.springframework.test.util.ReflectionTestUtils;

public class ClubCategoryFixture {

    public static ClubCategory 동아리_카테고리(Integer id, String name) {
        ClubCategory clubCategory = ClubCategory.builder()
            .name(name)
            .build();

        ReflectionTestUtils.setField(clubCategory, "id", id);

        return clubCategory;
    }
}
