package in.koreatech.koin.domain.ownershop.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ModifyEventRequest(
    @Schema(example = "감성떡볶이 이벤트합니다!!!", description = "제목")
    @NotBlank String title,

    @Schema(example = "OwnerShopsRequest", description = "이벤트 내용")
    String content,

    @Schema(description = "이벤트 이미지")
    String thumbnailImage,

    @Schema(example = "2024-10-24", description = "시작일")
    @NotNull LocalDate startDate,

    @Schema(example = "2024-11-24", description = "시작일")
    @NotNull LocalDate endDate
) {

}
