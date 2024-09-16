package in.koreatech.koin.admin.abtest.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("AbtestVariableCount")
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

    public void addCount() {
        count += 1;
    }

    public void minusCount() {
        count -= 1;
    }
}
