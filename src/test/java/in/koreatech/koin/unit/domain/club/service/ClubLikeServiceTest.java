package in.koreatech.koin.unit.domain.club.service;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.like.exception.ClubLikeDuplicateException;
import in.koreatech.koin.domain.club.like.exception.ClubLikeNotFoundException;
import in.koreatech.koin.domain.club.like.model.ClubLike;
import in.koreatech.koin.domain.club.like.repository.ClubLikeRepository;
import in.koreatech.koin.domain.club.like.service.ClubLikeService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubLikeServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClubLikeRepository clubLikeRepository;

    @InjectMocks
    private ClubLikeService clubLikeService;

    @Nested
    class LikeClub {

        Integer clubId;
        Integer userId;
        Club club;
        User user;

        @BeforeEach
        void init() {
            clubId = 1;
            userId = 1;

            club = spy(ClubFixture.활성화_BCSD_동아리(clubId));
            user = UserFixture.코인_유저();

            when(clubRepository.getByIdWithPessimisticLock(clubId)).thenReturn(club);
            when(userRepository.getById(userId)).thenReturn(user);
        }

        @Test
        void 유저가_동아리에_좋아요를_누르면_좋아요_수가_증가한다() {
            // given
            when(clubLikeRepository.existsByClubAndUser(club, user)).thenReturn(false);

            // when
            clubLikeService.likeClub(clubId, userId);

            // then
            ArgumentCaptor<ClubLike> likeCaptor = ArgumentCaptor.forClass(ClubLike.class);

            verify(clubLikeRepository).save(likeCaptor.capture());
            verify(club).increaseLikes();

            ClubLike clubLike = likeCaptor.getValue();

            assertThat(clubLike.getClub()).isEqualTo(club);
            assertThat(clubLike.getUser()).isEqualTo(user);
        }

        @Test
        void 이미_동아리에_좋아요를_눌렀다면_예외를_발생한다() {
            // given
            when(clubLikeRepository.existsByClubAndUser(club, user)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> clubLikeService.likeClub(clubId, userId))
                .isInstanceOf(ClubLikeDuplicateException.class)
                .hasMessage("이미 좋아요를 누른 동아리입니다!");
        }
    }

    @Nested
    class LikeClubCancel {

        Integer clubId;
        Integer userId;
        Club club;
        User user;

        @BeforeEach
        void init() {
            clubId = 1;
            userId = 1;

            club = spy(ClubFixture.활성화_BCSD_동아리(clubId));
            user = UserFixture.코인_유저();

            when(clubRepository.getByIdWithPessimisticLock(clubId)).thenReturn(club);
            when(userRepository.getById(userId)).thenReturn(user);
        }

        @Test
        void 좋아요를_누른_동아리에서_좋아요를_취소한다() {
            // given
            when(clubLikeRepository.existsByClubAndUser(club, user)).thenReturn(true);

            // when
            clubLikeService.likeClubCancel(clubId, userId);

            // then
            verify(clubLikeRepository).deleteByClubAndUser(club, user);
            verify(club).cancelLikes();
        }

        @Test
        void 좋아요를_누르지_않은_상태에서_취소를_하면_예외를_발생한다() {
            // given
            when(clubLikeRepository.existsByClubAndUser(club, user)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubLikeService.likeClubCancel(clubId, userId))
                .isInstanceOf(ClubLikeNotFoundException.class)
                .hasMessage("좋아요를 누른 적 없는 동아리입니다!");
        }
    }
}
