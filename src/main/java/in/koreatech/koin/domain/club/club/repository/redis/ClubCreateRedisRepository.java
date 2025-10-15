package in.koreatech.koin.domain.club.club.repository.redis;

import in.koreatech.koin.domain.club.club.model.redis.ClubCreateRedis;
import org.springframework.data.repository.CrudRepository;

public interface ClubCreateRedisRepository extends CrudRepository<ClubCreateRedis, String> {
}
