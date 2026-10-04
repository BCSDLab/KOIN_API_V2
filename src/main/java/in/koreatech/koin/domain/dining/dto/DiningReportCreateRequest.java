package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record DiningReportCreateRequest(
    @Schema(description = "공용 COOP 업로드의 file_url. 원본 URL을 그대로 보관합니다. "
        + "쿼리와 프래그먼트가 없는 upload/COOP/ 경로의 존재하는 파일이어야 합니다.",
        example = "https://static.koreatech.in/upload/COOP/2026/10/2/"
            + "e924c7d3-3757-4cd0-961b-e65c1f6cc8ad/soldout.jpg",
        format = "uri", minLength = 1, maxLength = 2048, requiredMode = REQUIRED)
    @NotBlank(message = "제보 사진 URL은 필수입니다.")
    @Size(max = 2048, message = "제보 사진 URL은 2048자 이하여야 합니다.")
    String imageUrl
) {
}
