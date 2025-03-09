package in.koreatech.koin.domain.bus.service.city.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.service.city.model.CityBusTimetable;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record CityBusTimetableResponse(
    @Schema(description = "업데이트 시각", example = "2024-07-18 18:00:00", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,

    @Schema(description = "버스 정보", example = """
        {
            "number": 400,
            "depart_node": "병천3리",
            "arrival_node": "종합터미널"
        }
        """, requiredMode = REQUIRED)
    BusInfo busInfo,

    @Schema(description = "버스 시간표", example = """
        [
            {
                "day_of_week": "평일",
                "depart_info": ["06:00", "13:12", "22:30"]
            },
            {
                "day_of_week": "주말",
                "depart_info": ["06:00", "13:12", "22:30"]
            },
            {
                "day_of_week": "공휴일",
                "depart_info": ["06:00", "13:12", "22:30"]
            },
            {
                "day_of_week": "임시",
                "depart_info": ["06:00", "13:12", "22:30"]
            }
        ]
        """, requiredMode = NOT_REQUIRED)
    List<BusTimetable> busTimetables
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record BusInfo(Long number, String departNode, String arrivalNode) {

        public static BusInfo of(Long number, CityBusDirection direction) {
            return new BusInfo(
                number,
                direction.getDepartNode(),
                direction.getApartNode()
            );
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record BusTimetable(String dayOfWeek, List<String> departInfo) {

        public static BusTimetable of(Long busNumber, CityBusDirection direction,
            CityBusTimetable.BusTimetable timetable) {
            return new BusTimetable(
                timetable.getDayOfWeek(),
                timetable.applyTimeOffset(busNumber, direction).stream()
                    .map(LocalTime::toString)
                    .toList()
            );
        }
    }

    public static CityBusTimetableResponse of(Long busNumber, CityBusDirection direction, CityBusTimetable timetable) {
        return new CityBusTimetableResponse(
            timetable.getUpdatedAt(),
            BusInfo.of(busNumber, direction),
            timetable.getBusTimetables().stream()
                .map(busTimetable -> BusTimetable.of(busNumber, direction, busTimetable))
                .toList()
        );
    }
}
