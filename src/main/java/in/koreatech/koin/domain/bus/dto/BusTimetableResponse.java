package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.service.model.BusTimetable;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record BusTimetableResponse(
    @Schema(description = "버스 시간표", example = """
        {
            "route_name": "주말(14시 35분)",
            "arrival_info": {
                "nodeName": "터미널(신세계 앞 횡단보도)",
                "arrivalTime": "14:35"
            }
        }
        """, requiredMode = NOT_REQUIRED)
    List<? extends BusTimetable> busTimetables,

    @Schema(description = "업데이트 시각", example = "2024-04-20 18:00:00", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {

}
