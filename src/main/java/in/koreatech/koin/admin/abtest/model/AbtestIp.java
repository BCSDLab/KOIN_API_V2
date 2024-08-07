package in.koreatech.koin.admin.abtest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("AbTestIp")
public class AbtestIp {

    @Id
    private Integer id;
    private List<Map<Integer, String>> ips = new ArrayList<>();

    @Builder
    private AbtestIp(Integer id, List<Map<Integer, String>> ips) {
        this.id = id;
        this.ips.addAll(ips);
    }
}
