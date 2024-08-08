package in.koreatech.koin.admin.abtest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("AbTestIp")
public class AbtestIp {

    // 실험 id
    @Id
    private Integer id;

    // 실험군 id : 사용자 ip
    private Map<Integer, String> ips = new HashMap<>();

    @Builder
    private AbtestIp(Integer id, Map<Integer, String> ips) {
        this.id = id;
        this.ips.putAll(ips);
    }

    public int getVariableIdByIp(String ipAddress) {
        return ips.entrySet().stream()
            .filter(entry -> Objects.equals(entry.getValue(), ipAddress))
            .findAny()
            .orElseThrow()
            .getKey();
    }
}
