package in.koreatech.koin.domain.member.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Track;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TrackResponse(
    @Schema(description = "트랙 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "트랙 명", example = "Backend", requiredMode = REQUIRED)
    String name,

    @Schema(description = "인원 수", example = "15", requiredMode = REQUIRED)
    Integer headcount,

    @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
    Boolean isDeleted,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
    LocalDateTime createdAt,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
    LocalDateTime updatedAt
) {

    public static TrackResponse from(Track track) {
        return new TrackResponse(
            track.getId(),
            track.getName(),
            track.getHeadcount(),
            track.isDeleted(),
            track.getCreatedAt(),
            track.getUpdatedAt()
        );
    }
}
