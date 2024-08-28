package in.koreatech.koin.admin.abtest.repository;

import static in.koreatech.koin.admin.abtest.model.redis.AbtestVariableIp.DELIMITER;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AbtestVariableIpTemplateRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void deleteByVariableId(Integer variableId) {
        redisTemplate.keys("AbtestVariableIp:" + variableId + DELIMITER + "*").forEach(redisTemplate::delete);
    }
}
