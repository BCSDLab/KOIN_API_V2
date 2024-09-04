package in.koreatech.koin.admin.abtest.model.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("AbtestVariableIp")
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

    //TODO: 이제 해야할거: VariableIp 캐시를 어떻게 Ip 제거하고 variableId, accessHistoryId로 유지할지 고민해보기
    public static AbtestVariableAssign of(Integer variableId, Integer accessHistoryId) {
        return AbtestVariableAssign.builder()
            .id(variableId + DELIMITER + accessHistoryId)
            .expiration(CACHE_EXPIRE_DAYS)
            .build();
    }
}
