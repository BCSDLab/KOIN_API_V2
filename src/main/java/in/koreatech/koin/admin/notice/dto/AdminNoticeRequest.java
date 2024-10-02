package in.koreatech.koin.admin.notice.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.model.Article;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminNoticeRequest(
    @NotNull(message = "작성자명은 필수입니다.")
    @Size(min = 1, max = 10, message = "작성자명은 1자 이상, 100자 이하로 입력해주세요.")
    @Schema(description = "작성자명", example = "홍길동", requiredMode = REQUIRED)
    String author,

    @NotNull(message = "제목은 필수입니다.")
    @Size(min = 1, max = 50, message = "제목은 1자 이상,50자 이하로 입력해주세요.")
    @Schema(description = "제목", example = "제목 예시", requiredMode = REQUIRED)
    String title,

    @NotNull(message = "본문은 필수입니다.")
    @Size(min = 1, max = 100, message = "본문은 1자 이상, 100자 이하로 입력해주세요.")
    @Schema(description = "본문 내용", example = "본문 내용 예시", requiredMode = REQUIRED)
    String content
){

}
