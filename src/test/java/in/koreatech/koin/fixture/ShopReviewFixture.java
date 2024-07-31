package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewImage;
import in.koreatech.koin.domain.shop.model.ShopReviewMenu;
import in.koreatech.koin.domain.shop.repository.ShopReviewImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewMenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewRepository;
import in.koreatech.koin.domain.user.model.Student;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class ShopReviewFixture {

    private final ShopReviewRepository shopReviewRepository;
    private final ShopReviewImageRepository shopReviewImageRepository;
    private final ShopReviewMenuRepository shopReviewMenuRepository;

    public ShopReviewFixture(
        ShopReviewRepository shopReviewRepository,
        ShopReviewImageRepository shopReviewImageRepository,
        ShopReviewMenuRepository shopReviewMenuRepository
    ) {
        this.shopReviewRepository = shopReviewRepository;
        this.shopReviewImageRepository = shopReviewImageRepository;
        this.shopReviewMenuRepository = shopReviewMenuRepository;
    }


    public ShopReview 리뷰_4점(Student student, Shop shop) {
        ShopReview shopReview = ShopReview.builder()
            .reviewer(student)
            .content("여기 진짜 맛있어요.")
            .rating(4)
            .shop(shop)
            .build();
        shopReview.getImages().add(ShopReviewImage.builder()
            .imageUrls("https://static.koreatech.in/example.png")
            .review(shopReview)
            .build());
        shopReview.getMenus().add(ShopReviewMenu.builder()
            .menuName("피자")
            .review(shopReview)
            .build());
        ShopReview savedShopReview = shopReviewRepository.save(shopReview);
        return savedShopReview;
    }
}
