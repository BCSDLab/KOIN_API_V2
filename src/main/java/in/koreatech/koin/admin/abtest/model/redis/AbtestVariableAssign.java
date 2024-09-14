package in.koreatech.koin.admin.abtest.model.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("AbtestVariableAssign")
public class AbtestVariableAssign {

    public static final String DELIMITER = ":";

    private static final long CACHE_EXPIRE_DAYS = 3L;

    @Id
    private String id;

    @TimeToLive(unit = TimeUnit.DAYS)
    private final Long expiration;

    @Builder
    private AbtestVariableAssign(String id, Long expiration) {
        this.id = id;
        this.expiration = expiration;
    }

    public static AbtestVariableAssign of(Integer variableId, Integer accessHistoryId) {
        return AbtestVariableAssign.builder()
            .id(variableId + DELIMITER + accessHistoryId)
            .expiration(CACHE_EXPIRE_DAYS)
            .build();
    }
}
