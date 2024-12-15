package in.koreatech.koin.domain.bus.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BusNoticeRepository {

    public static final String BUS_NOTICE_KEY = "busNoticeArticle";
    private final RedisTemplate<String, Object> redisTemplate;

    public Map<Object, Object> getBusNotice() {
        Map<Object, Object> article = redisTemplate.opsForHash().entries(BUS_NOTICE_KEY);

        if (article.isEmpty()) {
            return null;
        }

        return article;
    }
}
