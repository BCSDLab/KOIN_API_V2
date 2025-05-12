package in.koreatech.koin.domain.club.model.redis;

import org.springframework.data.redis.core.RedisHash;

import in.koreatech.koin.domain.club.model.Club;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("hotClub")
public class HotClub {

    @Id
    private String id;

    private String name;

    private String imageUrl;

    @Builder
    private HotClub(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public static HotClub from(Club club) {
        return HotClub.builder()
            .id("hot_club")
            .name(club.getName())
            .imageUrl(club.getImageUrl())
            .build();
    }
}
