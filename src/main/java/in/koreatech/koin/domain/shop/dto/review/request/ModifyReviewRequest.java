package in.koreatech.koin.domain.shop.dto.review.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.validation.NotBlankElement;
import in.koreatech.koin.global.validation.UniqueUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ModifyReviewRequest(
    @Schema(example = "4", description = "리뷰의 별점", requiredMode = REQUIRED)
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating,

    @Schema(example = "정말 맛있어요~!", description = "리뷰 내용", requiredMode = REQUIRED)
    String content,

    @Schema(example = """
        [ "https://static.koreatech.in/example.png" ]
        """, description = "이미지 URL 리스트", requiredMode = REQUIRED)
    @Size(max = 3, message = "이미지는 최대 3개까지 입력 가능합니다.")
    @UniqueUrl(message = "이미지 URL은 중복될 수 없습니다.")
    List<String> imageUrls,

    @Schema(example = "[\"치킨\", \"피자\"]", description = "메뉴 이름", requiredMode = REQUIRED)
    @NotBlankElement(message = "빈 요소가 존재할 수 없습니다.")
    List<String> menuNames
) {
    @JsonCreator
    public ModifyReviewRequest(
        @JsonProperty("rating") @NotNull @Min(1) @Max(5) Integer rating,
        @JsonProperty("content") String content,
        @JsonProperty("image_urls") List<String> imageUrls,
        @JsonProperty("menu_names") List<String> menuNames
    ) {
        this.rating = rating;
        this.content = content;
        this.imageUrls = imageUrls == null ? new ArrayList<>() : imageUrls;
        this.menuNames = menuNames == null ? new ArrayList<>() : menuNames;
    }
}
