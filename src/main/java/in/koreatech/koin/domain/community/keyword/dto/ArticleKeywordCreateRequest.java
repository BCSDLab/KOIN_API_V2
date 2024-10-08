package in.koreatech.koin.domain.community.keyword.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleKeywordCreateRequest (
    @Schema(example = "수강신청", description = "키워드 명", requiredMode = REQUIRED)
    @NotBlank(message = "키워드를 입력해주세요.")
    @Size(min = 2, max = 20, message = "2글자 이상 20글자 이하의 단어만 넣을 수 있어요")
    String keyword
) {

}
