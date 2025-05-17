package in.koreatech.koin.domain.club.model.redis;

import static in.koreatech.koin.domain.club.dto.request.CreateClubRequest.InnerClubManagerRequest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.RedisHash;

import in.koreatech.koin.domain.club.dto.request.CreateClubRequest;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubAdmin;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("ClubCreateRequest")
public class ClubCreateRedis {

    @Id
    private String id;

    private String name;

    private String imageUrl;

    private List<InnerClubManagerRequest> clubAdmins;

    private Integer clubCategoryId;

    private String location;

    private String description;

    private String instagram;

    private String googleForm;

    private String openChat;

    private String phoneNumber;

    private Integer requesterId;

    private LocalDateTime createdAt;

    @Builder
    private ClubCreateRedis(
        String id,
        String name,
        String imageUrl,
        List<InnerClubManagerRequest> clubAdmins,
        Integer clubCategoryId,
        String location,
        String description,
        String instagram,
        String googleForm,
        String openChat,
        String phoneNumber,
        Integer requesterId,
        LocalDateTime createdAt
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
        this.createdAt = createdAt;
    }

    public static ClubCreateRedis of(CreateClubRequest request, Integer requesterId) {
        return ClubCreateRedis.builder()
            .id(request.name())
            .name(request.name())
            .imageUrl(request.imageUrl())
            .clubAdmins(request.clubManagers())
            .clubCategoryId(request.clubCategoryId())
            .location(request.location())
            .description(request.description())
            .instagram(request.instagram())
            .googleForm(request.googleForm())
            .openChat(request.openChat())
            .phoneNumber(request.phoneNumber())
            .requesterId(requesterId)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public Club toClub(ClubCategory category) {
        return Club.builder()
            .name(this.name)
            .imageUrl(this.imageUrl)
            .clubCategory(category)
            .location(this.location)
            .description(this.description)
            .likes(0)
            .hits(0)
            .lastWeekHits(0)
            .active(false)
            .introduction("")
            .build();
    }

    public ClubAdmin toClubAdmin(Club club, User requester) {
        return ClubAdmin.builder()
            .club(club)
            .user(requester)
            .build();
    }
}
