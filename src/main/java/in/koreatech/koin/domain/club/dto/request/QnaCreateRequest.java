package in.koreatech.koin.domain.club.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubQna;
import in.koreatech.koin.domain.student.model.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record QnaCreateRequest(
    @Schema(description = "부모 qna id", example = "1", requiredMode = NOT_REQUIRED)
    Integer parentId,

    @Schema(description = "내용", example = "언제 모집하나요?", requiredMode = REQUIRED)
    @NotBlank(message = "내용은 공백일 수 없습니다")
    @Size(max = 255, message = "내용은 최대 255자 입니다.")
    String content
) {

    public ClubQna toClubQna(Club club, Student student, ClubQna parentQna, boolean isManager) {
        return ClubQna.builder()
            .club(club)
            .author(student)
            .parent(parentQna)
            .content(content)
            .isDeleted(false)
            .isManager(isManager)
            .build();
    }
}
