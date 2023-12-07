package in.koreatech.koin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import in.koreatech.koin.domain.Track;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TrackResponse {

    private Long id;
    private String name;
    private Integer headcount;
    private Boolean isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

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
