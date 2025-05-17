package in.koreatech.koin.admin.club.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminClubsResponse(
    @Schema(description = "조건에 해당하는 동아리 수", example = "10", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 동아리 중 현재 페이지에서 조회된 수", example = "5", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 동아리를 조회할 수 있는 최대 페이지", example = "2", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "배너 리스트", requiredMode = REQUIRED)
    List<InnerAdminClubResponse> clubs
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerAdminClubResponse(
        @Schema(description = "동아리 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "동아리 이름", example = "BCSD", requiredMode = REQUIRED)
        String name,

        @Schema(description = "동아리 사진 링크", example = "https://bcsdlab.com/static/img/logo.d89d9cc.png", requiredMode = REQUIRED)
        String imageUrl,

        @Schema(description = "동아리 관리자 리스트", requiredMode = REQUIRED)
        List<InnerClubManagerResponse> clubManagers,

        @Schema(description = "동아리 분과 카테고리", example = "학술", requiredMode = REQUIRED)
        String clubCategoryName,

        @Schema(description = "동아리 생성일", example = "25.03.23", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yy.MM.dd")
        LocalDate createdAt,

        @Schema(description = "활성화 여부", example = "true", requiredMode = REQUIRED)
        Boolean active
    ) {
        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerClubManagerResponse(
            @Schema(description = "동아리 관리자 이름", example = "정해성", requiredMode = REQUIRED)
            String name,

            @Schema(description = "동아리 관리자 ID", example = "basdlab", requiredMode = REQUIRED)
            String userId,

            @Schema(description = "동아리 관리자 전화번호", example = "01012345678", requiredMode = REQUIRED)
            String phoneNumber
        ) {
            private static InnerClubManagerResponse from(User user) {
                return new InnerClubManagerResponse(
                    user.getName(),
                    user.getUserId(),
                    user.getPhoneNumber()
                );
            }
        }

        private static InnerAdminClubResponse from(Club club) {
            return new InnerAdminClubResponse(
                club.getId(),
                club.getName(),
                club.getImageUrl(),
                club.getClubManagers().stream()
                    .map(clubAdmin -> InnerClubManagerResponse.from(clubAdmin.getUser()))
                    .toList(),
                club.getClubCategory().getName(),
                club.getCreatedAt().toLocalDate(),
                club.getActive()
            );
        }
    }

    public static AdminClubsResponse from(Page<Club> clubs) {
        return new AdminClubsResponse(
            clubs.getTotalElements(),
            clubs.getContent().size(),
            clubs.getTotalPages(),
            clubs.getNumber() + 1,
            clubs.getContent().stream()
                .map(InnerAdminClubResponse::from)
                .collect(Collectors.toList())
        );
    }
}
