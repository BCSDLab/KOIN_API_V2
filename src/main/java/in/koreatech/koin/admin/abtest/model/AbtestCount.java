package in.koreatech.koin.admin.abtest.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("AbTestCount")
public class AbtestCount {

    @Id
    private Integer id;
    private Integer count;

    @Builder
    private AbtestCount(Integer id, Integer count) {
        this.id = id;
        this.count = count;
    }
}
