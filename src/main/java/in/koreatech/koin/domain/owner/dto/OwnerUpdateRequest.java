package in.koreatech.koin.domain.owner.dto;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerUpdateRequest(
    @Valid
    @Size(min = 3, max = 5, message = "이미지는 사업자등록증, 영업신고증, 통장사본을 포함하여 최소 3개 최대 5개까지 가능합니다.")
    @Schema(description = "첨부 파일 URL 목록(최소 3개 최대 5개, 코인 파일 형식이어야 함)")
    List<InnerAttachmentUrlRequest> attachmentUrls
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerAttachmentUrlRequest(
        @NotBlank
        @URL(protocol = "https", regexp = ".*static\\.koreatech\\.in.*", message = "코인 파일 저장 형식이 아닙니다.")
        @Schema(description = "첨부 파일 URL (코인 파일 형식이어야 함)", example = "https://static.koreatech.in/1.png")
        String fileUrl
    ) {

    }
}
