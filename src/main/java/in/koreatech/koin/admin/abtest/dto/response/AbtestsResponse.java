package in.koreatech.koin.admin.abtest.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestsResponse(
    @Schema(description = "AB테스트 목록", required = true)
    List<InnerAbtestResponse> tests,

    @Schema(example = "85", description = "전체 실험 수", required = true)
    Long totalCount,

    @Schema(example = "10", description = "현재 페이지 실험 수", required = true)
    Integer currentCount,

    @Schema(example = "9", description = "전체 페이지 수", required = true)
    Integer totalPage,

    @Schema(example = "1", description = "현재 페이지", required = true)
    Integer currentPage
) {

    public static AbtestsResponse of(Page<Abtest> pagedResult, Criteria criteria) {
        return new AbtestsResponse(
            pagedResult.stream()
                .map(InnerAbtestResponse::from)
                .toList(),
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerAbtestResponse(

        @Schema(example = "1", description = "AB테스트 고유 id", required = true)
        Integer id,

        @Schema(example = "IN_PROGRESS", description = "AB테스트 상태", required = true)
        String status,

        @Schema(example = "A", description = "승자 실험군 이름", required = true)
        String winnerName,

        @Schema(example = "송선권", description = "AB테스트 생성자", required = true)
        String creator,

        @Schema(example = "campus", description = "AB테스트 팀명", required = true)
        String team,

        @Schema(example = "식단 테스트", description = "AB테스트 제목", required = true)
        String displayTitle,

        @Schema(example = "dining_ui_test", description = "AB테스트 제목(변수명)", required = true)
        String title,

        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {
        public static InnerAbtestResponse from(Abtest abtest) {
            return new InnerAbtestResponse(
                abtest.getId(),
                abtest.getStatus().name(),
                abtest.getWinnerName(),
                abtest.getCreator(),
                abtest.getTeam(),
                abtest.getDisplayTitle(),
                abtest.getTitle(),
                abtest.getCreatedAt(),
                abtest.getUpdatedAt()
            );
        }
    }
}
