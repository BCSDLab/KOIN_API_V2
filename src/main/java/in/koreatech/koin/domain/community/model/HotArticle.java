package in.koreatech.koin.domain.community.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@RedisHash(value = "hot-article")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotArticle implements Comparable<HotArticle> {

    private static final Long EXPIRED_DAYS = 1L;

    @Id
    Long id;

    Long hit;

    List<LocalDateTime> expiredTimes = new ArrayList<>();

    public void hit() {
        hit++;
        expiredTimes.add(getNewExpiredTime());
    }

    public void validate() {
        int beforeTimeSize = expiredTimes.size();
        expiredTimes.removeIf(expiredTime -> LocalDateTime.now().isAfter(expiredTime));
        int removedTimeCount = beforeTimeSize - expiredTimes.size();
        hit -= removedTimeCount;
    }

    public boolean isEmpty() {
        return hit == 0;
    }

    private static LocalDateTime getNewExpiredTime() {
        return LocalDateTime.now().plusDays(EXPIRED_DAYS);
    }

    public static HotArticle from(Long articleId) {
        return builder()
            .id(articleId)
            .hit(1L)
            .expiredTimes(List.of(getNewExpiredTime()))
            .build();
    }

    @Builder
    public HotArticle(Long id, Long hit, List<LocalDateTime> expiredTimes) {
        this.id = id;
        this.hit = hit;
        this.expiredTimes = expiredTimes;
    }

    @Override
    public int compareTo(HotArticle other) {
        return Long.compare(hit, other.hit);
    }
}
