package in.koreatech.koin.domain.shop.dto.review.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewImage;
import in.koreatech.koin.domain.shop.model.review.ShopReviewMenu;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopReviewResponse(

    @Schema(example = "1", description = "리뷰 ID", requiredMode = REQUIRED)
    int reviewId,

    @Schema(example = "4", description = "별점", requiredMode = REQUIRED)
    int rating,

    @Schema(example = "익명23432423", description = "닉네임", requiredMode = REQUIRED)
    String nickName,

    @Schema(example = "맛있어요~", description = "내용", requiredMode = REQUIRED)
    String content,

    @Schema(example = """
        [ "https://static.koreatech.in/example.png" ]
        """, description = "이미지 URL 리스트", requiredMode = REQUIRED)
    List<String> imageUrls,

    @Schema(example = """
        [ "피자" ]
        """, description = "메뉴 이름 리스트", requiredMode = REQUIRED)
    List<String> menuNames,

    @Schema(example = "false", description = "수정된 적 있는 리뷰인지", requiredMode = REQUIRED)
    boolean isModified,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2024-03-01", description = "리뷰 작성일", requiredMode = REQUIRED)
    LocalDateTime createdAt
) {

    public static ShopReviewResponse from(ShopReview review) {
        String nickName = review.getReviewer().getUser().getNickname();
        if (nickName == null) {
            nickName = review.getReviewer().getUser().getAnonymousNickname();
        }
        return new ShopReviewResponse(
            review.getId(),
            review.getRating(),
            nickName,
            review.getContent(),
            review.getImages().stream().map(ShopReviewImage::getImageUrls).toList(),
            review.getMenus().stream().map(ShopReviewMenu::getMenuName).toList(),
            !review.getCreatedAt().equals(review.getUpdatedAt()),
            review.getCreatedAt()
        );
    }
}
