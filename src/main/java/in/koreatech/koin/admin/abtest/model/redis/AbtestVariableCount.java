package in.koreatech.koin.admin.abtest.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("AbTestCount")
public class AbtestVariableCount {

    @Id
    private Integer variableId;
    private Integer count;

    @Builder
    private AbtestVariableCount(Integer variableId, Integer count) {
        this.variableId = variableId;
        this.count = count;
    }

    public void resetCount() {
        count = 0;
    }
}
