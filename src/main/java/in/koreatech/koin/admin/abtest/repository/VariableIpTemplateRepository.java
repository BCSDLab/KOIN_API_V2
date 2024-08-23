package in.koreatech.koin.admin.abtest.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VariableIpTemplateRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void deleteByVariableId(Integer variableId) {
        redisTemplate.keys("VariableIp:" + variableId + ":*").forEach(redisTemplate::delete);
    }
}
