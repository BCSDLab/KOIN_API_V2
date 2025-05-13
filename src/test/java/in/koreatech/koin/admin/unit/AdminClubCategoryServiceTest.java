package in.koreatech.koin.admin.unit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.admin.club.dto.response.AdminClubCategoriesResponse;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.admin.club.service.AdminClubCategoryService;
import in.koreatech.koin.domain.club.model.ClubCategory;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
public class AdminClubCategoryServiceTest {

    @InjectMocks
    private AdminClubCategoryService adminClubCategoryService;

    @Mock
    private AdminClubCategoryRepository adminClubCategoryRepository;


    @Test
    void 등록된_동아리_카테고리를_조회한다() {
        //given
        List<ClubCategory> categories = List.of(
            ClubCategory.builder().id(1).name("학술").build(),
            ClubCategory.builder().id(2).name("운동").build()
        );
        when(adminClubCategoryRepository.findAll()).thenReturn(categories);

        //when
        AdminClubCategoriesResponse response = adminClubCategoryService.getClubCategories();

        //then
        assertAll(
            () -> assertEquals(2, response.clubCategories().size()),
            () -> assertEquals("학술", response.clubCategories().get(0).name()),
            () -> assertEquals("운동", response.clubCategories().get(1).name())
        );

        verify(adminClubCategoryRepository, times(1)).findAll();
    }
}
