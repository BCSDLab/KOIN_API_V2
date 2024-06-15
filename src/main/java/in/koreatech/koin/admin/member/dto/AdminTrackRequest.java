package in.koreatech.koin.admin.member.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Track;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminTrackRequest(
    @Schema(description = "트랙 고유 ID", example = "1")
    Integer id,

    @Schema(description = "트랙 명", example = "Backend", requiredMode = REQUIRED)
    @NotBlank(message = "트랙명은 비워둘 수 없습니다.")
    String name,

    @Schema(description = "인원 수", example = "15")
    @NotNull(message = "인원 수는 비워둘 수 없습니다.")
    Integer headcount,

    @Schema(description = "삭제 여부", example = "false")
    boolean isDeleted
) {

    public Track toEntity() {
        return Track.builder()
            .name(name)
            .headcount(headcount)
            .isDeleted(isDeleted)
            .build();
    }
}
