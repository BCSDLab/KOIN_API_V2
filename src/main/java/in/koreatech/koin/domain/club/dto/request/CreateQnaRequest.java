package in.koreatech.koin.domain.club.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubQna;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record CreateQnaRequest(
    @Schema(description = "부모 qna id", example = "1", requiredMode = NOT_REQUIRED)
    Integer parentId,

    @Schema(description = "내용", example = "언제 모집하나요?", requiredMode = REQUIRED)
    @NotBlank(message = "내용은 공백일 수 없습니다")
    @Size(max = 255, message = "내용은 최대 255자 입니다.")
    String content
) {

    public ClubQna toClubQna(Club club, User user, ClubQna parentQna) {
        return ClubQna.builder()
            .club(club)
            .user(user)
            .parent(parentQna)
            .content(content)
            .isDeleted(false)
            .build();
    }
}
