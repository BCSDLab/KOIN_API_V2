package in.koreatech.koin.admin.abtest.repository;

import static in.koreatech.koin.admin.abtest.model.redis.AbtestVariableAssign.DELIMITER;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AbtestVariableAssignTemplateRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void deleteAllByVariableId(Integer variableId) {
        Set<String> keys = redisTemplate.keys("AbtestVariableAssign:" + variableId + DELIMITER + "*");
        if (keys == null) {
            return;
        }
        for (String s : keys) {
            redisTemplate.delete(s);
        }
    }
}
