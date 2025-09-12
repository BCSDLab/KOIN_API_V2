package in.koreatech.koin.unit.fixture;

import in.koreatech.koin.domain.club.category.model.ClubCategory;

public class ClubCategoryFixture {

    private ClubCategoryFixture() { }

    public static ClubCategory 동아리_카테고리(Integer id, String name) {
        return ClubCategory.builder()
            .id(id)
            .name(name)
            .build();
    }
}
