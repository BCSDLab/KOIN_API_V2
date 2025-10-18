package in.koreatech.koin.domain.club.like.service;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.like.exception.ClubLikeDuplicateException;
import in.koreatech.koin.domain.club.like.exception.ClubLikeNotFoundException;
import in.koreatech.koin.domain.club.like.model.ClubLike;
import in.koreatech.koin.domain.club.like.repository.ClubLikeRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubLikeService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ClubLikeRepository clubLikeRepository;

    @Transactional
    public void likeClub(Integer clubId, Integer userId) {
        Club club = clubRepository.getByIdWithPessimisticLock(clubId);
        User user = userRepository.getById(userId);

        boolean alreadyLiked = clubLikeRepository.existsByClubAndUser(club, user);
        if (alreadyLiked) {
            throw ClubLikeDuplicateException.withDetail(clubId);
        }

        ClubLike clubLike = ClubLike.builder()
            .club(club)
            .user(user)
            .build();

        clubLikeRepository.save(clubLike);
        club.increaseLikes();
    }

    @Transactional
    public void likeClubCancel(Integer clubId, Integer userId) {
        Club club = clubRepository.getByIdWithPessimisticLock(clubId);
        User user = userRepository.getById(userId);

        boolean alreadyLiked = clubLikeRepository.existsByClubAndUser(club, user);
        if (!alreadyLiked) {
            throw ClubLikeNotFoundException.withDetail(club.getId());
        }

        clubLikeRepository.deleteByClubAndUser(club, user);
        club.cancelLikes();
    }
}
