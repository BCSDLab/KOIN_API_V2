package in.koreatech.koin.admin.abtest.model.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("AbtestVariableIp")
public class AbtestVariableIp {

    public static final String DELIMITER = "/";

    private static final long CACHE_EXPIRE_DAYS = 3L;

    @Id
    private String id;

    @TimeToLive(unit = TimeUnit.DAYS)
    private final Long expiration;

    @Builder
    private AbtestVariableIp(String id, Long expiration) {
        this.id = id;
        this.expiration = expiration;
    }

    public static AbtestVariableIp of(Integer variableId, String ip) {
        return AbtestVariableIp.builder()
            .id(variableId + DELIMITER + ip)
            .expiration(CACHE_EXPIRE_DAYS)
            .build();
    }

/*
    public int getVariableIdByIp(String ipAddress) {
        return ips.entrySet().stream()
            .filter(entry -> Objects.equals(entry.getValue(), ipAddress))
            .findAny()
            .orElseThrow()
            .getKey();
    }
*/
}
