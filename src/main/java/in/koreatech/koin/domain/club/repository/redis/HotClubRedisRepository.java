package in.koreatech.koin.domain.club.repository.redis;

import org.springframework.data.repository.CrudRepository;

import in.koreatech.koin.domain.club.model.redis.HotClub;

public interface HotClubRedisRepository extends CrudRepository<HotClub, String> {

}
