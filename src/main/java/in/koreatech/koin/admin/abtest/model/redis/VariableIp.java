package in.koreatech.koin.admin.abtest.model.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("VariableIp")
public class VariableIp {

    /**
     * MEMO
     * 특정 실험에서 특정 아이피의 실험군을 조회하고자 한다면?
     * 해당 실험에 속한 모든 실험군을 stream으로 순회하며 각각에 대해
     * variableId:ip 꼴로 variableIpRepository.existsById(String id) 한다.
     * -> findByVariableIdAndIp()
     */

    private static final long CACHE_EXPIRE_DAYS = 3L;

    @Id
    private String id;

    @TimeToLive(unit = TimeUnit.DAYS)
    private final Long expiration;

    @Builder
    private VariableIp(String id, Long expiration) {
        this.id = id;
        this.expiration = expiration;
    }

    public static VariableIp of(Integer variableId, String ip) {
        return VariableIp.builder()
            .id(variableId + ":" + ip)
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
