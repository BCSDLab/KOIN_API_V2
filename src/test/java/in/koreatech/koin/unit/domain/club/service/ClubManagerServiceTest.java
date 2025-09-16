package in.koreatech.koin.unit.domain.club.service;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.manager.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.manager.exception.ClubManagerAlreadyException;
import in.koreatech.koin.domain.club.manager.model.ClubManager;
import in.koreatech.koin.domain.club.manager.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.manager.service.ClubManagerService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.unit.fixture.ClubFixture;
import in.koreatech.koin.unit.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClubManagerServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubManagerRepository clubManagerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClubManagerService clubManagerService;

    @Nested
    class EmpowermentClubManager {

        Integer clubId;
        Integer currentManagerId;
        Integer changedManagerId;
        Club club;
        User currentManager;
        User changedManager;
        ClubManagerEmpowermentRequest request;

        @BeforeEach
        void init() {
            clubId = 1;
            currentManagerId = 1;
            changedManagerId = 2;
            club = ClubFixture.활성화_BCSD_동아리(clubId);
            currentManager = UserFixture.코인_유저();
            changedManager = UserFixture.코인_유저();
            request = new ClubManagerEmpowermentRequest(clubId, changedManager.getLoginId());

            ReflectionTestUtils.setField(currentManager, "id", currentManagerId);
            ReflectionTestUtils.setField(changedManager, "id", changedManagerId);

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(userRepository.getById(currentManagerId)).thenReturn(currentManager);
            when(userRepository.getByLoginIdAndUserTypeIn(changedManager.getLoginId(), UserType.KOIN_STUDENT_TYPES))
                .thenReturn(changedManager);
        }

        @Test
        void 관리자_권한_위임에_성공한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, currentManagerId)).thenReturn(true);
            when(clubManagerRepository.existsByClubAndUser(club, changedManager)).thenReturn(false);

            // when
            clubManagerService.empowermentClubManager(request, currentManagerId);

            // then
            ArgumentCaptor<ClubManager> clubManagerCaptor = ArgumentCaptor.forClass(ClubManager.class);

            verify(clubManagerRepository).deleteByClubAndUser(club, currentManager);
            verify(clubManagerRepository).save(clubManagerCaptor.capture());

            ClubManager newClubManager = clubManagerCaptor.getValue();

            assertThat(newClubManager.getClub()).isEqualTo(club);
            assertThat(newClubManager.getUser()).isEqualTo(changedManager);
        }

        @Test
        void 관리자가_아닌_유저가_권한_위임을_할_경우_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, currentManagerId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubManagerService.empowermentClubManager(request, currentManagerId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }

        @Test
        void 권한_위임_대상이_이미_관리자면_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, currentManagerId)).thenReturn(true);
            when(clubManagerRepository.existsByClubAndUser(club, changedManager)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> clubManagerService.empowermentClubManager(request, currentManagerId))
                .isInstanceOf(ClubManagerAlreadyException.class)
                .hasMessage("이미 동아리의 관리자입니다.");
        }
    }
}
