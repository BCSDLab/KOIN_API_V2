package in.koreatech.koin.domain.club.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.exception.ClubHotNotFoundException;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHotRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubHotRedisRepository hotClubRedisRepository;
    private final ClubHotRepository clubHotRepository;

    public ClubHotResponse getHotClub() {
        return hotClubRedisRepository.findById(ClubHotRedis.REDIS_KEY)
            .map(ClubHotResponse::from)
            .orElseGet(this::getHotClubFromDBAndCache);
    }

    private ClubHotResponse getHotClubFromDBAndCache() {
        return clubHotRepository.findTopByOrderByEndDateDesc()
            .map(clubHot -> {
                hotClubRedisRepository.save(ClubHotRedis.from(clubHot.getClub()));
                return ClubHotResponse.from(clubHot.getClub());
            })
            .orElseThrow(() -> ClubHotNotFoundException.withDetail(""));
    }
}
