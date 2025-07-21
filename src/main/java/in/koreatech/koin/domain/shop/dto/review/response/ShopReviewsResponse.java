package in.koreatech.koin.domain.shop.dto.review.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DISMISSED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewImage;
import in.koreatech.koin.domain.shop.model.review.ShopReviewMenu;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopReviewsResponse(
    @Schema(description = "총 리뷰 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "리뷰 중에 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "리뷰를 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "해당 상점의 리뷰 통계", requiredMode = REQUIRED)
    InnerReviewStatisticsResponse statistics,

    @Schema(description = "해당 상점의 리뷰", requiredMode = REQUIRED)
    List<InnerReviewResponse> reviews
) {

    public static ShopReviewsResponse from(
        Page<ShopReview> pagedResult,
        Integer userId,
        Criteria criteria,
        Map<Integer, Integer> ratings
    ) {
        List<ShopReview> reviews = pagedResult.getContent();
        return new ShopReviewsResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            InnerReviewStatisticsResponse.from(ratings),
            reviews.stream().map(review -> InnerReviewResponse.from(review, userId)).toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerReviewResponse(
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

        @Schema(example = "false", description = "로그인한 회원의 리뷰인지 여부. 비회원이라면 false", requiredMode = REQUIRED)
        boolean isMine,

        @Schema(example = "false", description = "수정된 적 있는 리뷰인지", requiredMode = REQUIRED)
        boolean isModified,

        @Schema(example = "true", description = "신고 여부", requiredMode = REQUIRED)
        boolean isReported,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2024-03-01", description = "리뷰 작성일", requiredMode = REQUIRED)
        LocalDateTime createdAt
    ) {

        public static InnerReviewResponse from(ShopReview review, Integer userId) {
            String nickName = review.getReviewer().getUser().getNickname();
            if (nickName == null) {
                nickName = review.getReviewer().getUser().getAnonymousNickname();
            }
            boolean isReported = review.getReports().stream()
                .filter(it -> it.getReportStatus() != DISMISSED)
                .count() > 0;
            return new InnerReviewResponse(
                review.getId(),
                review.getRating(),
                nickName,
                review.getContent(),
                review.getImages().stream().map(ShopReviewImage::getImageUrls).toList(),
                review.getMenus().stream().map(ShopReviewMenu::getMenuName).toList(),
                Objects.equals(review.getReviewer().getId(), userId),
                !review.getCreatedAt().equals(review.getUpdatedAt()),
                isReported,
                review.getCreatedAt()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerReviewStatisticsResponse(
        @Schema(example = "3.0", description = "해당 상점의 리뷰 평점", requiredMode = REQUIRED)
        double averageRating,

        @Schema(example = """
                  {
                      "1": 1,
                      "2": 0,
                      "3": 0,
                      "4": 2,
                      "5": 0
                  }
            """, description = "해당 상점의 리뷰 통계", requiredMode = Schema.RequiredMode.REQUIRED)
        Map<Integer, Integer> ratings

    ) {

        public static InnerReviewStatisticsResponse from(Map<Integer, Integer> ratings) {
            double totalSum = ratings.entrySet()
                .stream()
                .mapToDouble(entry -> entry.getKey() * entry.getValue())
                .sum();
            int totalCount = ratings.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
            double averageRating = 0.0;
            if (totalCount > 0) {
                averageRating = totalSum / totalCount;
            }
            return new InnerReviewStatisticsResponse(
                Math.round(averageRating * 10) / 10.0,
                ratings
            );
        }
    }
}
