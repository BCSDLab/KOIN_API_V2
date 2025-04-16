package in.koreatech.koin.admin.history.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.history.model.AdminActivityHistory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminHistoriesResponse(
    @Schema(description = "조건에 해당하는 히스토리 수", example = "10", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 히스토리 중 현재 페이지에서 조회된 수", example = "5", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 히스토리를 조회할 수 있는 최대 페이지", example = "2", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "어드민 계정 리스트", requiredMode = REQUIRED)
    List<InnerAdminHistoriesResponse> histories
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerAdminHistoriesResponse(
        @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "도메인 엔티티 id", example = "null", requiredMode = REQUIRED)
        Integer domainId,

        @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
        String name,

        @Schema(description = "도메인 이름", example = "코인 공지", requiredMode = REQUIRED)
        String domainName,

        @Schema(description = "HTTP 요청 메소드 종류", example = "생성", requiredMode = REQUIRED)
        String requestMethod,

        @Schema(description = "HTTP 요청 메시지 바디", example = """
            {
                "title": "제목 예시",
                "content": "본문 내용 예시"
            }
            """,
            requiredMode = REQUIRED
        )
        String requestMessage,

        @Schema(description = "요청 시간", example = "2019-08-16 23:01:52", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
    ) {
        public static InnerAdminHistoriesResponse from(AdminActivityHistory adminActivityHistory) {
            return new InnerAdminHistoriesResponse(
                adminActivityHistory.getId(),
                adminActivityHistory.getDomainId(),
                adminActivityHistory.getAdmin().getName(),
                adminActivityHistory.getDomainName().getDescription(),
                adminActivityHistory.getRequestMethod().getValue(),
                adminActivityHistory.getRequestMessage(),
                adminActivityHistory.getCreatedAt()
            );
        }
    }

    public static AdminHistoriesResponse from(Page<AdminActivityHistory> adminActivityHistoryPage) {
        return new AdminHistoriesResponse(
            adminActivityHistoryPage.getTotalElements(),
            adminActivityHistoryPage.getContent().size(),
            adminActivityHistoryPage.getTotalPages(),
            adminActivityHistoryPage.getNumber() + 1,
            adminActivityHistoryPage.getContent().stream()
                .map(InnerAdminHistoriesResponse::from)
                .collect(Collectors.toList())
        );
    }
}
