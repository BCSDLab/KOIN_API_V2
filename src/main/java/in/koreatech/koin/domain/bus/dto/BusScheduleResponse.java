package in.koreatech.koin.domain.bus.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.enums.BusStation;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record BusScheduleResponse(
    @Schema(description = "출발 정류장", example = "KOREATECH", requiredMode = REQUIRED)
    BusStation depart,
    @Schema(description = "도착 정류장", example = "TERMINAL", requiredMode = REQUIRED)
    BusStation arrival,
    @Schema(description = "출발 날짜", example = "2024-11-05", requiredMode = REQUIRED)
    LocalDate departDate,
    @Schema(description = "출발 시간", example = "12:00", requiredMode = REQUIRED)
    LocalTime departTime,
    @Schema(description = "교통편 조회 결과", requiredMode = NOT_REQUIRED)
    List<ScheduleInfo> schedule

) {
    @JsonNaming(SnakeCaseStrategy.class)
    public record ScheduleInfo(
        @Schema(description = "버스 타입 (shuttle, express, city)", example = "express", requiredMode = REQUIRED)
        String busType,
        @Schema(description = "버스 이름 또는 노선명", example = "대성티앤이", requiredMode = REQUIRED)
        String busName,
        @Schema(description = "버스 출발 시간", example = "16:50", requiredMode = REQUIRED)
        LocalTime departTime
    ) {

        public static Comparator<ScheduleInfo> compareBusType() {
            List<String> priority = List.of("shuttle", "express", "city");
            return Comparator.comparingInt(schedule -> priority.indexOf(schedule.busType));
        }
    }
}
