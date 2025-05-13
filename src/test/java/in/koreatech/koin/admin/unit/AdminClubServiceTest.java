package in.koreatech.koin.admin.unit;

import static in.koreatech.koin.admin.club.dto.request.ModifyAdminClubRequest.InnerClubAdminUpdateRequest;
import static in.koreatech.koin.domain.club.enums.SNSType.INSTAGRAM;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.admin.club.dto.request.CreateAdminClubRequest;
import in.koreatech.koin.admin.club.dto.request.ModifyAdminClubRequest;
import in.koreatech.koin.admin.club.dto.response.AdminClubResponse;
import in.koreatech.koin.admin.club.repository.AdminClubAdminRepository;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.admin.club.repository.AdminClubRepository;
import in.koreatech.koin.admin.club.service.AdminClubService;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubAdmin;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.model.ClubSNS;
import in.koreatech.koin.domain.user.model.User;

@SuppressWarnings({"NonAsciiCharacters", "unchecked"})
@ExtendWith(MockitoExtension.class)
public class AdminClubServiceTest {

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

    @Test
    void adminClubResponse_으로_동아리_상세_조회한다() {
        // given
        User user = User.builder()
            .password("1234")
            .name("정해성")
            .userType(STUDENT)
            .email("bcsdlab@koreatech.ac.kr")
            .userId("bcsdlab")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        ClubSNS sns = ClubSNS.builder()
            .snsType(INSTAGRAM)
            .contact("https://www.instagram.com/bcsdlab/")
            .build();

        Club club = Club.builder()
            .id(1)
            .name("BCSD")
            .imageUrl("https://bcsdlab.com/static/img/logo.d89d9cc.png")
            .likes(9999)
            .description("즐겁게 일하고 열심히 노는 IT 특성화 동아리")
            .clubCategory(ClubCategory.builder().name("학술").build())
            .location("학생회관")
            .active(TRUE)
            .build();
        when(adminClubRepository.getById(1)).thenReturn(club);

        ClubAdmin clubAdmin = ClubAdmin.builder()
            .club(club)
            .user(user)
            .build();

        ReflectionTestUtils.setField(club, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(club, "updatedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(club, "clubAdmins", List.of(clubAdmin));
        ReflectionTestUtils.setField(club, "clubSNSs", List.of(sns));

        // when
        AdminClubResponse response = adminClubService.getClub(1);

        // then
        assertAll(
            () -> assertEquals(1, response.id()),
            () -> assertEquals("BCSD", response.name()),
            () -> assertEquals("https://bcsdlab.com/static/img/logo.d89d9cc.png", response.imageUrl()),
            () -> assertEquals("학술", response.clubCategoryName()),
            () -> assertEquals(9999, response.likes()),
            () -> assertEquals("즐겁게 일하고 열심히 노는 IT 특성화 동아리", response.description()),
            () -> assertEquals(1, response.clubAdmins().size()),
            () -> assertEquals("정해성", response.clubAdmins().get(0).name()),
            () -> assertEquals("bcsdlab@koreatech.ac.kr", response.clubAdmins().get(0).email()),
            () -> assertEquals(1, response.snsContacts().size()),
            () -> assertEquals("인스타그램", response.snsContacts().get(0).snsType()),
            () -> assertEquals("https://www.instagram.com/bcsdlab/", response.snsContacts().get(0).contract()),
            () -> assertEquals(LocalDateTime.now().toLocalDate(), response.createdAt()),
            () -> assertEquals(TRUE, response.active())
        );

        verify(adminClubRepository).getById(1);
    }

    @Test
    void modifyAdminClubRequest_를_이용해서_동아리_정보를_수정한다() {
        // given
        ModifyAdminClubRequest request = new ModifyAdminClubRequest(
            "BCSD Lab",
            "https://bcsdlab.com/static/img/logo.d89d9cc.png",
            "학술",
            List.of(new InnerClubAdminUpdateRequest("koin")),
            "학생회관",
            "즐겁게 일하고 열심히 노는 IT 특성화 동아리",
            FALSE
        );

        ClubCategory clubCategory = ClubCategory.builder()
            .name("학술")
            .build();
        when(adminClubCategoryRepository.getByName("학술")).thenReturn(clubCategory);

        User perUser = User.builder()
            .password("1234")
            .name("최준호")
            .userType(STUDENT)
            .email("bcsd@koreatech.ac.kr")
            .userId("bcsd")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        User postUser = User.builder()
            .password("1234")
            .name("정해성")
            .userType(STUDENT)
            .email("koin@koreatech.ac.kr")
            .userId("koin")
            .isAuthed(true)
            .isDeleted(false)
            .build();
        when(adminUserRepository.getByUserId("koin")).thenReturn(postUser);

        ClubSNS sns = ClubSNS.builder()
            .snsType(INSTAGRAM)
            .contact("https://www.instagram.com/bcsdlab/")
            .build();

        Club club = Club.builder()
            .id(1)
            .name("BCSD")
            .imageUrl("https://bcsdlab.com/static/img/logo.d89d9cc.png")
            .likes(9999)
            .description("즐겁게 일하고 열심히 노는 IT 특성화 동아리")
            .clubCategory(ClubCategory.builder().name("학술").build())
            .location("학생회관")
            .active(TRUE)
            .build();
        when(adminClubRepository.getById(1)).thenReturn(club);

        List<ClubAdmin> clubAdmins = List.of(ClubAdmin.builder().club(club).user(perUser).build());

        ReflectionTestUtils.setField(club, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(club, "updatedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(club, "clubAdmins", clubAdmins);
        ReflectionTestUtils.setField(club, "clubSNSs", List.of(sns));

        // when
        adminClubService.modifyClub(1, request);

        // then
        verify(adminClubCategoryRepository).getByName("학술");
        verify(adminClubRepository).getById(1);
        verify(adminUserRepository).getByUserId("koin");
        verify(adminClubAdminRepository).deleteAllByClub(club);

        ArgumentCaptor<List<ClubAdmin>> clubAdminCaptor = ArgumentCaptor.forClass(List.class);
        verify(adminClubAdminRepository).saveAll(clubAdminCaptor.capture());
        List<ClubAdmin> updateClubAdmins = clubAdminCaptor.getValue();

        assertAll(
            () -> assertEquals(1, updateClubAdmins.size()),
            () -> assertEquals("koin", updateClubAdmins.get(0).getUser().getUserId())
        );
    }
}
