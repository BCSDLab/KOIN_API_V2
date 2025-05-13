package in.koreatech.koin.admin.unit;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.admin.club.dto.request.CreateAdminClubRequest;
import in.koreatech.koin.admin.club.repository.AdminClubAdminRepository;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.admin.club.repository.AdminClubRepository;
import in.koreatech.koin.admin.club.service.AdminClubService;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubAdmin;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.user.model.User;

@SuppressWarnings({"NonAsciiCharacters", "unchecked"})
@ExtendWith(MockitoExtension.class)
public class AdminClubTest {

    @InjectMocks
    private AdminClubService adminClubService;

    @Mock
    private AdminClubRepository adminClubRepository;

    @Mock
    private AdminClubCategoryRepository adminClubCategoryRepository;

    @Mock
    private AdminClubAdminRepository adminClubAdminRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Test
    void createAdminClubRequest_를_이용해서_동아리를_생성한다() {
        // given
        CreateAdminClubRequest request = new CreateAdminClubRequest(
            "BCSD Lab",
            "https://bcsdlab.com/static/img/logo.d89d9cc.png",
            List.of(new CreateAdminClubRequest.InnerClubAdminRequest("bcsdlab")),
            "학술",
            "학생회관",
            "즐겁게 일하고 열심히 노는 IT 특성화 동아리"
        );

        ClubCategory clubCategory = ClubCategory.builder()
            .name("학술")
            .build();
        when(adminClubCategoryRepository.getByName("학술")).thenReturn(clubCategory);

        User user = User.builder()
            .password("1234")
            .userType(STUDENT)
            .email("bcsdlab@koreatech.ac.kr")
            .userId("bcsdlab")
            .isAuthed(true)
            .isDeleted(false)
            .build();
        when(adminUserRepository.getByUserId("bcsdlab")).thenReturn(user);

        when(adminClubRepository.save(any(Club.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        adminClubService.createClub(request);

        // then
        verify(adminClubCategoryRepository).getByName("학술");
        verify(adminUserRepository).getByUserId("bcsdlab");

        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        verify(adminClubRepository).save(clubCaptor.capture());

        Club capturedClub = clubCaptor.getValue();
        assertAll(
            () -> assertEquals("BCSD Lab", capturedClub.getName()),
            () -> assertEquals("https://bcsdlab.com/static/img/logo.d89d9cc.png", capturedClub.getImageUrl()),
            () -> assertEquals("학생회관", capturedClub.getLocation()),
            () -> assertEquals("즐겁게 일하고 열심히 노는 IT 특성화 동아리", capturedClub.getDescription()),
            () -> assertEquals(clubCategory, capturedClub.getClubCategory())
        );

        ArgumentCaptor<List<ClubAdmin>> clubAdminCaptor = ArgumentCaptor.forClass(List.class);
        verify(adminClubAdminRepository).saveAll(clubAdminCaptor.capture());

        List<ClubAdmin> capturedAdmins = clubAdminCaptor.getValue();
        assertAll(
            () -> assertEquals(1, capturedAdmins.size()),
            () -> assertEquals(user, capturedAdmins.get(0).getUser()),
            () -> assertEquals(capturedClub, capturedAdmins.get(0).getClub())
        );
    }
}
