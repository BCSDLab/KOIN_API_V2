package in.koreatech.koin.domain.member.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Track;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TrackResponse(
    @Schema(description = "트랙 고유 ID", example = "1")
    Integer id,

    @Schema(description = "트랙 명", example = "Backend")
    String name,

    @Schema(description = "인원 수", example = "15")
    Integer headcount,

    @Schema(description = "삭제 여부", example = "false")
    Boolean isDeleted,

    @Schema(description = "생성 일자", example = "2023-01-04 12:00:01")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

    @Schema(description = "수정 일자", example = "2023-01-04 12:00:01")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
) {

    public static TrackResponse from(Track track) {
        return new TrackResponse(
            track.getId(),
            track.getName(),
            track.getHeadcount(),
            track.getIsDeleted(),
            track.getCreatedAt(),
            track.getUpdatedAt()
        );
    }
}
