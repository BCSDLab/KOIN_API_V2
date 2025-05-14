package in.koreatech.koin.domain.club.model.redis;

import java.util.List;

import org.springframework.data.redis.core.RedisHash;

import in.koreatech.koin.domain.club.dto.request.CreateClubRequest;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("createClub")
public class ClubCreateRedis {

    public static final String REDIS_KEY = "create_club";

    @Id
    private String id;

    private String name;

    private String imageUrl;

    private List<CreateClubRequest.InnerClubAdminRequest> clubAdmins;

    private Integer clubCategoryId;

    private String location;

    private String description;

    private String instagram;

    private String googleForm;

    private String openChat;

    private String phoneNumber;

    private Integer requesterId;

    @Builder
    private ClubCreateRedis(
        String id,
        String name,
        String imageUrl,
        List<CreateClubRequest.InnerClubAdminRequest> clubAdmins,
        Integer clubCategoryId,
        String location,
        String description,
        String instagram,
        String googleForm,
        String openChat,
        String phoneNumber,
        Integer requesterId
    ) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.clubAdmins = clubAdmins;
        this.clubCategoryId = clubCategoryId;
        this.location = location;
        this.description = description;
        this.instagram = instagram;
        this.googleForm = googleForm;
        this.openChat = openChat;
        this.phoneNumber = phoneNumber;
        this.requesterId = requesterId;
    }

    public static ClubCreateRedis of(CreateClubRequest request, Integer requesterId) {
        return ClubCreateRedis.builder()
            .id(REDIS_KEY + ":" + request.name())
            .name(request.name())
            .imageUrl(request.imageUrl())
            .clubAdmins(request.clubAdmins())
            .clubCategoryId(request.clubCategoryId())
            .location(request.location())
            .description(request.description())
            .instagram(request.instagram())
            .googleForm(request.googleForm())
            .openChat(request.openChat())
            .phoneNumber(request.phoneNumber())
            .requesterId(requesterId)
            .build();
    }
}
