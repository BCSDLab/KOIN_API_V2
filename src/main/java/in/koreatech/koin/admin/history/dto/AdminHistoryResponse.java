package in.koreatech.koin.admin.history.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.history.enums.DomainType;
import in.koreatech.koin.admin.history.enums.HttpMethodType;
import in.koreatech.koin.admin.history.model.AdminActivityHistory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminHistoryResponse(
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
    public static AdminHistoryResponse from(AdminActivityHistory adminActivityHistory) {
        return new AdminHistoryResponse(
            adminActivityHistory.getId(),
            adminActivityHistory.getDomainId(),
            adminActivityHistory.getAdmin().getUser().getName(),
            adminActivityHistory.getDomainName().getDescription(),
            adminActivityHistory.getRequestMethod().getValue(),
            adminActivityHistory.getRequestMessage(),
            adminActivityHistory.getCreatedAt()
        );
    }
}
