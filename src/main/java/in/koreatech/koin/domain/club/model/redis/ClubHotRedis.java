package in.koreatech.koin.domain.club.model.redis;

import org.springframework.data.redis.core.RedisHash;

import in.koreatech.koin.domain.club.model.Club;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("hotClub")
public class ClubHotRedis {

    public static final String REDIS_KEY = "hot_club";

    @Id
    private String id;

    private String name;

    private String imageUrl;

    @Builder
    private ClubHotRedis(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public static ClubHotRedis from(Club club) {
        return ClubHotRedis.builder()
            .id(REDIS_KEY)
            .name(club.getName())
            .imageUrl(club.getImageUrl())
            .build();
    }
}
