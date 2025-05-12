package in.koreatech.koin.domain.club.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.redis.HotClub;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.repository.redis.HotClubRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubScheduleService {

    private final ClubRepository clubRepository;
    private final HotClubRedisRepository hotClubRedisRepository;

    @Transactional
    public void updateHotClub() {
        List<Club> clubs = clubRepository.findAll();
        clubs.stream()
            .max(Comparator.comparingInt(Club::getHitsIncrease))
            .map(HotClub::from)
            .ifPresent(hotClubRedisRepository::save);
        clubs.forEach(Club::updateLastWeekHits);
    }
}
