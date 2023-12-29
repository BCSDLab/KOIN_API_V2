package in.koreatech.koin.domain.track.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.track.model.Track;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TrackResponse(
    Long id,
    String name,
    Integer headcount,
    Boolean isDeleted,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
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
