package in.koreatech.koin.admin.shop.dto.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewImage;
import in.koreatech.koin.domain.shop.model.review.ShopReviewMenu;
import in.koreatech.koin.domain.shop.model.review.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminShopsReviewsResponse(

    @Schema(description = "총 상점의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "리뷰 리스트", requiredMode = REQUIRED)
    List<InnerReviewResponse> reviews
) {

    public static AdminShopsReviewsResponse of(Page<ShopReview> reviews, Criteria criteria) {
        return new AdminShopsReviewsResponse(
            reviews.getTotalElements(),
            reviews.getNumberOfElements(),
            criteria.getPage(),
            reviews.stream().map(InnerReviewResponse::from).toList()
        );
    }

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

        @Schema(example = "false", description = "수정된 적 있는 리뷰인지", requiredMode = REQUIRED)
        boolean isModified,

        @Schema(example = "false", description = "핸들링 되지 않은 신고가 있는 리뷰인지", requiredMode = REQUIRED)
        boolean isHaveUnhandledReport,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2024-03-01", description = "리뷰 작성일", requiredMode = REQUIRED)
        LocalDateTime createdAt,

        @Schema(description = "리뷰에 대한 신고 리스트")
        List<InnerReportResponse> reports,

        @Schema(description = "상점 정보", requiredMode = REQUIRED)
        InnerShopResponse shop
    ) {

        public static InnerReviewResponse from(ShopReview review) {
            String nickName = Optional.ofNullable(review.getReviewer().getUser().getNickname())
                                      .orElse(review.getReviewer().getAnonymousNickname());

            return new InnerReviewResponse(
                review.getId(),
                review.getRating(),
                nickName,
                review.getContent(),
                review.getImages().stream().map(ShopReviewImage::getImageUrls).toList(),
                review.getMenus().stream().map(ShopReviewMenu::getMenuName).toList(),
                !review.getCreatedAt().equals(review.getUpdatedAt()),
                review.getReports().stream().anyMatch(report -> report.getReportStatus() == UNHANDLED),
                review.getCreatedAt(),
                review.getReports().stream().map(InnerReportResponse::from).toList(),
                InnerShopResponse.from(review.getShop())
            );
        }

        public record InnerReportResponse(
            @Schema(example = "1", description = "신고 ID", requiredMode = REQUIRED)
            int reportId,

            @Schema(example = "부적절한 내용", description = "신고 제목", requiredMode = REQUIRED)
            String title,

            @Schema(example = "이 리뷰는 욕설을 포함하고 있습니다.", description = "신고 내용", requiredMode = REQUIRED)
            String content,

            @Schema(example = "user1234", description = "신고자 닉네임", requiredMode = REQUIRED)
            String nickName,

            @Schema(example = "PENDING", description = "신고 상태", requiredMode = REQUIRED)
            String status
        ) {

            public static InnerReportResponse from(ShopReviewReport report) {
                return new InnerReportResponse(
                    report.getId(),
                    report.getTitle(),
                    report.getContent(),
                    report.getUserId().getUser().getNickname(),
                    report.getReportStatus().name()
                );
            }
        }

        public record InnerShopResponse(
            @Schema(example = "1", description = "상점 ID", requiredMode = REQUIRED)
            int shopId,

            @Schema(example = "맛집", description = "상점 이름", requiredMode = REQUIRED)
            String shopName
        ) {

            public static InnerShopResponse from(Shop shop) {
                return new InnerShopResponse(
                    shop.getId(),
                    shop.getName()
                );
            }
        }
    }
}
