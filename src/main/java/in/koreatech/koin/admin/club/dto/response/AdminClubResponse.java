package in.koreatech.koin.admin.club.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubSNS;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminClubResponse(
    @Schema(description = "동아리 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "동아리 이름", example = "BCSD", requiredMode = REQUIRED)
    String name,

    @Schema(description = "동아리 사진 링크", example = "https://bcsdlab.com/static/img/logo.d89d9cc.png", requiredMode = REQUIRED)
    String imageUrl,

    @Schema(description = "동아리 관리자 리스트", requiredMode = REQUIRED)
    List<InnerClubAdminResponse> clubAdmins,

    @Schema(description = "동아리 분과 카테고리", example = "학술", requiredMode = REQUIRED)
    String clubCategoryName,

    @Schema(description = "동아리 좋아요 개수", example = "9999", requiredMode = REQUIRED)
    Integer likes,

    @Schema(description = "동아리 소개", example = "즐겁게 일하고 열심히 노는 IT 특성화 동아리", requiredMode = REQUIRED)
    String description,

    @Schema(description = "동아리 연락처 리스트", requiredMode = REQUIRED)
    List<InnerClubSNSResponse> snsContacts,

    @Schema(description = "배너 생성일", example = "25.03.23", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yy.MM.dd")
    LocalDate createdAt,

    @Schema(description = "활성화 여부", example = "true", requiredMode = REQUIRED)
    Boolean active
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerClubAdminResponse(
        @Schema(description = "동아리 관리자 이름", example = "정해성", requiredMode = REQUIRED)
        String name,

        @Schema(description = "동아리 관리자 이메일", example = "bcsdlab@gmail.com", requiredMode = REQUIRED)
        String email
    ) {
        private static InnerClubAdminResponse from(User user) {
            return new InnerClubAdminResponse(
                user.getName(),
                user.getEmail()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerClubSNSResponse(
        @Schema(description = "동아리 SNS 종류", example = "인스타", requiredMode = REQUIRED)
        String snsType,

        @Schema(description = "동아리 SNS 연락처", example = "https://www.instagram.com/bcsdlab/", requiredMode = REQUIRED)
        String contract
    ) {
        private static InnerClubSNSResponse from(ClubSNS clubSNS) {
            return new InnerClubSNSResponse(
                clubSNS.getSnsType().getDisplayName(),
                clubSNS.getContact()
            );
        }
    }

    public static AdminClubResponse from(Club club) {
        return new AdminClubResponse(
            club.getId(),
            club.getName(),
            club.getImageUrl(),
            club.getClubAdmins().stream()
                .map(clubAdmin -> InnerClubAdminResponse.from(clubAdmin.getUser()))
                .toList(),
            club.getClubCategory().getName(),
            club.getLikes(),
            club.getDescription(),
            club.getClubSNSs().stream()
                .map(InnerClubSNSResponse::from)
                .toList(),
            club.getCreatedAt().toLocalDate(),
            club.getActive()
        );
    }
}
