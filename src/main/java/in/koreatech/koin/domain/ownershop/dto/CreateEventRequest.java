package in.koreatech.koin.domain.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CreateEventRequest(
    @Schema(example = "감성떡볶이 이벤트합니다!!!", description = "제목", requiredMode = REQUIRED)
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @Schema(example = "감성떡볶이 이벤트합니다!!! 많은관심 부탁드려요! 감성을 한스푼 더 얹어드립니다", description = "이벤트 내용", requiredMode = REQUIRED)
    @NotBlank(message = "내용은 필수입니다.")
    String content,

    @Schema(description = "이벤트 이미지", example = """
        [ "https://testimage.com", "https://testimage2.com" ]
        """, requiredMode = REQUIRED)
    @NotNull(message = "이벤트 이미지는 필수입니다.")
    @Size(min = 0, max = 3, message = "사진은 최대 3개까지 입력 가능합니다.")
    List<String> thumbnailImages,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2024-10-24", description = "시작일", requiredMode = REQUIRED)
    @NotNull(message = "시작일은 필수입니다.")
    LocalDate startDate,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2024-11-24", description = "종료일", requiredMode = REQUIRED)
    @NotNull(message = "종료일은 필수입니다.")
    LocalDate endDate
) {

}
