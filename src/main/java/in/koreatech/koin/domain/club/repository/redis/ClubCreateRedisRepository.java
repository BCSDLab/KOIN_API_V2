package in.koreatech.koin.domain.club.repository.redis;

import org.springframework.data.repository.CrudRepository;

import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;

public interface ClubCreateRedisRepository extends CrudRepository<ClubCreateRedis, String> {
}
