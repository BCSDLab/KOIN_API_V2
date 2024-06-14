package in.koreatech.koin.admin.member.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Track;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminTrackRequest(
    @Schema(description = "트랙 명", example = "Backend", requiredMode = REQUIRED)
    String name,

    @Schema(description = "인원 수", example = "15", requiredMode = REQUIRED)
    Integer headcount,

    @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
    Boolean isDeleted
) {

    public Track toEntity() {
        return Track.builder()
            .name(name)
            .headcount(headcount)
            .isDeleted(isDeleted)
            .build();
    }
}
