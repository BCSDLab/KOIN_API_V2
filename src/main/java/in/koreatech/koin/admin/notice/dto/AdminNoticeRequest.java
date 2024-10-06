package in.koreatech.koin.admin.notice.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminNoticeRequest(
    @NotNull(message = "제목은 필수입니다.")
    @Size(min = 1, max = 255, message = "제목은 1자 이상, 255자 이하로 입력해주세요.")
    @Schema(description = "제목", example = "제목 예시", requiredMode = REQUIRED)
    String title,

    @NotNull(message = "본문은 필수입니다.")
    @Schema(description = "본문 내용", example = "본문 내용 예시", requiredMode = REQUIRED)
    String content
){

}
