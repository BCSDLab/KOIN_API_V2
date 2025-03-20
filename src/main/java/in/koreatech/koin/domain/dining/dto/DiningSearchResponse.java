package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record DiningSearchResponse(
    @Schema(description = "조건에 해당하는 총 식단의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 식단 중에 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 식단들을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "식단 정보 리스트", requiredMode = REQUIRED)
    List<DiningInformation> dinings
) {
    public static DiningSearchResponse of(Page<Dining> pagedResult, Criteria criteria){
        return new DiningSearchResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() +1,
            pagedResult.getContent()
                .stream()
                .map(DiningInformation::from)
                .toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record DiningInformation(
        @Schema(description = "메뉴 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "식단 제공 날짜", example = "2024-03-11", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @Schema(description = "식사 시간", example = "LUNCH", requiredMode = REQUIRED)
        String type,

        @Schema(description = "식단 제공 장소", example = "A코스", requiredMode = REQUIRED)
        String place,

        @Schema(description = "칼로리", example = "790", requiredMode = NOT_REQUIRED)
        Integer kcal,

        @Schema(description = "식단", example = """
        ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "고구마순들깨볶음", "총각김치", "생야채샐러드&D", "누룽지탕"]
        """, requiredMode = REQUIRED)
        List<String> menu,

        @Schema(description = "이미지 URL", example = "https://stage.koreatech.in/image.jpg", requiredMode = NOT_REQUIRED)
        String imageUrl,

        @Schema(description = "생성 일자", example = "2024-03-15 14:02:48", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "품절 시각", example = "2024-04-04 23:01:52", requiredMode = NOT_REQUIRED)
        LocalDateTime soldoutAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "메뉴 변경 시각", example = "2024-04-04 23:01:52", requiredMode = NOT_REQUIRED)
        LocalDateTime changedAt,

        @Schema(description = "식단 좋아요 수", example = "1", requiredMode = REQUIRED)
        Integer likes
    ) {
        public static DiningInformation from(Dining dining) {
            return new DiningInformation(
                dining.getId(),
                dining.getDate(),
                dining.getType().name(),
                dining.getPlace(),
                dining.getKcal(),
                dining.getMenu(),
                dining.getImageUrl(),
                dining.getCreatedAt(),
                dining.getCreatedAt(),
                dining.getCreatedAt(),
                dining.getLikes()
            );
        }
    }
}

