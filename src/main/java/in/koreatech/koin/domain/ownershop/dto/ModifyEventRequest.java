package in.koreatech.koin.domain.ownershop.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ModifyEventRequest(
    @Schema(example = "감성떡볶이 이벤트합니다!!!", description = "제목")
    @NotBlank String title,

    @Schema(example = "OwnerShopsRequest", description = "이벤트 내용")
    String content,

    @Schema(description = "이벤트 이미지")
    @Size(min = 0, max = 3, message = "사진은 최대 3개까지 입력 가능합니다.")
    List<String> thumbnailImages,

    @Schema(example = "2024-10-24", description = "시작일")
    @NotNull LocalDate startDate,

    @Schema(example = "2024-11-24", description = "시작일")
    @NotNull LocalDate endDate
) {

}
