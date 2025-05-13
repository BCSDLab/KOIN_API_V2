package in.koreatech.koin.domain.club.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubHot;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHotRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubScheduleService {

    private final ClubRepository clubRepository;
    private final ClubHotRedisRepository hotClubRedisRepository;
    private final ClubHotRepository clubHotRepository;

    @Transactional
    public void updateHotClub() {
        List<Club> clubs = clubRepository.findAll();
        Optional<Club> topClub = clubs.stream()
            .max(Comparator.comparingInt(Club::getHitsIncrease));

        if (topClub.isPresent()) {
            Club club = topClub.get();
            ClubHot clubHot = ClubHot.builder()
                .club(club)
                .rank(1)
                .periodHits(club.getHitsIncrease())
                .startDate(getStartOfLastWeek())
                .endDate(getEndOfLastWeek())
                .build();
            clubHotRepository.save(clubHot);
            hotClubRedisRepository.save(ClubHotRedis.from(club));
        }

        clubs.forEach(Club::updateLastWeekHits);
    }

    private LocalDate getStartOfLastWeek() {
        return LocalDate.now()
            .minusWeeks(1)
            .with(DayOfWeek.MONDAY);
    }

    private LocalDate getEndOfLastWeek() {
        return LocalDate.now()
            .minusWeeks(1)
            .with(DayOfWeek.SUNDAY);
    }
}
