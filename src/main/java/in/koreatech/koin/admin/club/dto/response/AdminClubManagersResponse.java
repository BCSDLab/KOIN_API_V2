package in.koreatech.koin.admin.club.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubManager;
import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminClubManagersResponse(
    @Schema(description = "조건에 해당하는 동아리 관리자의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 동아리 관리자 중에 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 동아리 관리자를 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "동아리 리스트", requiredMode = REQUIRED)
    List<InnerClubManagersResponse> clubs
) {
    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerClubManagersResponse(
        @Schema(description = "동아리 아이디", example = "1", requiredMode = REQUIRED)
        Integer clubId,

        @Schema(description = "동아리 관리자 이름", example = "배진호", requiredMode = REQUIRED)
        String clubManagerName,

        @Schema(description = "동아리 관리자 전화번호", example = "01024607469", requiredMode = REQUIRED)
        String phoneNumber,

        @Schema(description = "동아리 생성일", example = "25.03.23", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yy.MM.dd")
        LocalDateTime createdAt,

        @Schema(description = "동아리 이름", example = "BCSD", requiredMode = REQUIRED)
        String clubName
    ) {
        public static InnerClubManagersResponse from(ClubManager clubManager) {
            Club club = clubManager.getClub();
            User user = clubManager.getUser();

            return new InnerClubManagersResponse(
                club.getId(),
                user.getName(),
                user.getPhoneNumber(),
                club.getCreatedAt(),
                club.getName()
            );
        }

        public static InnerClubManagersResponse fromRedis(ClubCreateRedis redis, User requester) {
            return new InnerClubManagersResponse(
                null,
                requester.getName(),
                requester.getPhoneNumber(),
                redis.getCreatedAt(),
                redis.getName()
            );
        }
    }

    public static AdminClubManagersResponse of(Page<ClubManager> pagedResult, Criteria criteria) {
        return new AdminClubManagersResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent().stream()
                .map(InnerClubManagersResponse::from)
                .toList()
        );
    }
}
