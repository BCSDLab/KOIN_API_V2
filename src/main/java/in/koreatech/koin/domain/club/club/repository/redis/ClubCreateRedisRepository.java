package in.koreatech.koin.domain.club.club.repository.redis;

import org.springframework.data.repository.CrudRepository;

import in.koreatech.koin.domain.club.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.config.repository.RedisRepository;

@RedisRepository
public interface ClubCreateRedisRepository extends CrudRepository<ClubCreateRedis, String> {
}
