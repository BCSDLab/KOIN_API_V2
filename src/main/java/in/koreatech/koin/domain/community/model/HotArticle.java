package in.koreatech.koin.domain.community.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@RedisHash(value = "hot-article")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class HotArticle {

    private static final Long EXPIRED_DAYS = 1L;

    @Id
    Long id;

    Long hit;

    List<LocalDateTime> expiredTimes = new ArrayList<>();

    public void hit() {
        hit++;
        // expiredTimes.add(LocalDateTime.now().plusDays(EXPIRED_DAYS));
        expiredTimes.add(LocalDateTime.now().plusSeconds(10L));
        updateHit();
    }

    public Long getHit() {
        updateHit();
        return hit;
    }

    private void updateHit() {
        int beforeTimeSize = expiredTimes.size();
        expiredTimes.removeIf(expiredTime -> LocalDateTime.now().isAfter(expiredTime));
        int removedTimeCount = beforeTimeSize - expiredTimes.size();
        hit -= removedTimeCount;
    }

    public static HotArticle from(Long articleId) {
        return new HotArticle(articleId, 1L, List.of(LocalDateTime.now()));
    }
}
