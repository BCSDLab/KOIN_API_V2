package in.koreatech.koin.domain.shop.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ReviewImage;
import in.koreatech.koin.domain.shop.model.ReviewMenu;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ShopReviewResponse(
    int count,
    List<InnerReviewResponse> innerReviewResponses,
    InnerReviewStatisticsResponse innerReviewStatisticsResponse
) {
    public static ShopReviewResponse from(List<ShopReview> review) {
        return new ShopReviewResponse(
            review.size(),
            review.stream().map(InnerReviewResponse::from).toList(),
            InnerReviewStatisticsResponse.from(review)
        );
    }

    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record InnerReviewResponse(
        int reviewId,
        int rating,
        String nickName,
        String content,
        List<String> imageUrls,
        List<String> menuNames,
        LocalDateTime createdAt
    ) {
        public static InnerReviewResponse from(ShopReview review) {
            return new InnerReviewResponse(
                review.getId(),
                review.getRating(),
                review.getReviewer().getNickname(),
                review.getContent(),
                review.getImages().stream().map(ReviewImage::getImageUrls).toList(),
                review.getMenus().stream().map(ReviewMenu::getMenuName).toList(),
                review.getCreatedAt()
            );
        }
    }

    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record InnerReviewStatisticsResponse(
        Double averageRating,
        Map<Integer, Integer> statistics
    ) {
        public static InnerReviewStatisticsResponse from(List<ShopReview> reviews) {
            double averageRating = reviews.stream()
                .mapToInt(ShopReview::getRating)
                .average()
                .orElse(0.0);
            Map<Integer, Integer> statistics = Map.of(
                1, 0,
                2, 0,
                3, 0,
                4, 0,
                5, 0
            );
            reviews.stream().forEach(review ->
                statistics.put(
                    review.getRating(),
                    statistics.put(review.getRating(), statistics.get(review.getRating()) + 1)
                )
            );
            return new InnerReviewStatisticsResponse(
                averageRating,
                statistics
            );
        }
    }
}
