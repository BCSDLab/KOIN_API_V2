package in.koreatech.koin.domain.club.club.repository.redis;

import org.springframework.data.repository.CrudRepository;

import in.koreatech.koin.domain.club.club.model.redis.ClubHotRedis;
import in.koreatech.koin.global.config.repository.RedisRepository;

@RedisRepository
public interface ClubHotRedisRepository extends CrudRepository<ClubHotRedis, String> {

}
