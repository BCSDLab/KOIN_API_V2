package in.koreatech.koin.admin.coopShop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminCoopSemesterResponse(
    @Schema(description = "학기 고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "학기", example = "24-2학기", requiredMode = REQUIRED)
    String semester,

    @Schema(description = "학기 기간 시작일", example = "2024-09-02", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fromDate,

    @Schema(description = "학기 기간 마감일", example = "2024-12-20", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate toDate,

    @Schema(description = "업데이트 일자", example = "2024-09-01 14:02:48", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,

    @Schema(description = "생협 매장 정보 리스트", requiredMode = REQUIRED)
    List<InnerCoopShop> coopShops
) {
    public static AdminCoopSemesterResponse from(CoopSemester coopSemester) {
        return new AdminCoopSemesterResponse(
            coopSemester.getId(),
            coopSemester.getSemester(),
            coopSemester.getFromDate(),
            coopSemester.getToDate(),
            coopSemester.getUpdatedAt(),
            coopSemester.getCoopShops().stream().map(InnerCoopShop::from).toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCoopShop(
        @Schema(example = "1", description = "생협 매장 고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(example = "세탁소", description = "생협 매장 이름", requiredMode = REQUIRED)
        String name,

        @Schema(description = "요일별 생협 매장 운영시간")
        List<InnerCoopOpens> opens,

        @Schema(example = "041-560-1234", description = "생협 매장 연락처", requiredMode = REQUIRED)
        String phone,

        @Schema(example = "학생식당 2층", description = "생협 매장 위치", requiredMode = REQUIRED)
        String location,

        @Schema(example = "공휴일 휴무", description = "생협 매장 특이사항")
        String remarks
    ) {

        public static InnerCoopShop from(CoopShop coopShop) {
            return new InnerCoopShop(
                coopShop.getId(),
                coopShop.getCoopName().getName(),
                coopShop.getCoopOpens().stream()
                    .map(InnerCoopOpens::from)
                    .toList(),
                coopShop.getPhone(),
                coopShop.getLocation(),
                coopShop.getRemarks()
            );
        }

        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerCoopOpens(

            @Schema(example = "평일", description = "생협 매장 운영시간 요일", requiredMode = REQUIRED)
            String dayOfWeek,

            @Schema(example = "아침", description = "생협 매장 운영시간 타입", requiredMode = REQUIRED)
            String type,

            @Schema(example = "09:00", description = "생협 매장 오픈 시간", requiredMode = REQUIRED)
            String openTime,

            @Schema(example = "18:00", description = "생협 매장 마감 시간", requiredMode = REQUIRED)
            String closeTime
        ) {

            public static InnerCoopOpens from(CoopOpen coopOpen) {
                return new InnerCoopOpens(
                    coopOpen.getDayOfWeek().getDay(),
                    coopOpen.getType(),
                    coopOpen.getOpenTime(),
                    coopOpen.getCloseTime()
                );
            }
        }
    }
}
