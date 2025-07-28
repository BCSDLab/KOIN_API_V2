package in.koreatech.koin.admin.land.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminLandsResponse(

    @Schema(description = "조건에 해당하는 총 집의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 집 중에 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 집들을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "집 정보 리스트", requiredMode = REQUIRED)
    List<SimpleLandInformation> lands
) {
    public static AdminLandsResponse of(Page<Land> pagedResult, Criteria criteria) {
        return new AdminLandsResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent()
                .stream()
                .map(SimpleLandInformation::from)
                .toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record SimpleLandInformation(
        @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이름", example = "금실타운", requiredMode = REQUIRED)
        String name,

        @Schema(description = "종류", example = "원룸")
        String roomType,

        @Schema(description = "월세", example = "200만원 (6개월)")
        String monthlyFee,

        @Schema(description = "전세", example = "3500")
        String charterFee,

        @Schema(description = "삭제(soft delete) 여부", example = "false", requiredMode = REQUIRED)
        Boolean isDeleted
    ) {
        public static SimpleLandInformation from(Land land) {
            return new SimpleLandInformation(
                land.getId(),
                land.getName(),
                land.getRoomType(),
                land.getMonthlyFee(),
                land.getCharterFee(),
                land.isDeleted()
            );
        }
    }
}
