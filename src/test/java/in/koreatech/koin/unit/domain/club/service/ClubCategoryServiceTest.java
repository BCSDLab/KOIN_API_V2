package in.koreatech.koin.unit.domain.club.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import in.koreatech.koin.unit.fixture.ClubCategoryFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.club.dto.response.ClubCategoriesResponse;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.service.ClubCategoryService;

@ExtendWith(MockitoExtension.class)
public class ClubCategoryServiceTest {

    @Mock
    private ClubCategoryRepository clubCategoryRepository;

    @InjectMocks
    private ClubCategoryService clubCategoryService;

    @Test
    void 동아리의_모든_카테고리를_조회한다() {
        // given
        List<ClubCategory> clubCategories = List.of(
            ClubCategoryFixture.동아리_카테고리(1, "학술"),
            ClubCategoryFixture.동아리_카테고리(2, "운동")
        );
        when(clubCategoryRepository.findAll()).thenReturn(clubCategories);

        // when
        ClubCategoriesResponse response = clubCategoryService.getClubCategories();

        // then
        assertThat(response).isNotNull();
        assertThat(response.clubCategories()).hasSize(2);

        assertThat(response.clubCategories().get(0).id()).isEqualTo(1);
        assertThat(response.clubCategories().get(0).name()).isEqualTo("학술");

        assertThat(response.clubCategories().get(1).id()).isEqualTo(2);
        assertThat(response.clubCategories().get(1).name()).isEqualTo("운동");

        verify(clubCategoryRepository).findAll();
    }
}
