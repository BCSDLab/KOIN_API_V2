package in.koreatech.koin.domain.coop.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "excelDownload")
public class ExcelDownloadCache {
    
    private static final long CACHE_EXPIRE_MINUTES = 30L;

    @Id
    private String id;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private final Long expiration;

    @Builder
    private ExcelDownloadCache(String id, Long expiration) {
        this.id = id;
        this.expiration = expiration;
    }

    public static ExcelDownloadCache from(String id){
        return ExcelDownloadCache.builder()
            .id(id)
            .expiration(CACHE_EXPIRE_MINUTES)
            .build();
    }
}
