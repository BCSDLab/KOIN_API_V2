package in.koreatech.koin.admin.coopShop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.global.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminCoopSemestersResponse(

    @Schema(description = "조건에 해당하는 총 학기 수", example = "20", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 학기 중 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 학기를 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "생협 매장 정보 리스트", requiredMode = REQUIRED)
    List<InnerSemester> coopShopSemesters
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerSemester(
        Integer id,
        String semester,
        LocalDate fromDate,
        LocalDate toDate
    ) {

        public static InnerSemester from(CoopSemester coopSemester) {
            return new InnerSemester(
                coopSemester.getId(),
                coopSemester.getSemester(),
                coopSemester.getFromDate(),
                coopSemester.getToDate()
            );
        }
    }

    public static AdminCoopSemestersResponse of(Page<CoopSemester> pagedResult, Criteria criteria) {
        return new AdminCoopSemestersResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent()
                .stream()
                .map(InnerSemester::from)
                .toList()
        );
    }
}
