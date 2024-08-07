package in.koreatech.koin.fixture;

import java.time.Clock;

import org.springframework.stereotype.Component;

import in.koreatech.koin.config.FixedDate;
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
public class ShopReviewFixture {

    private final ShopReviewRepository shopReviewRepository;
    private final ShopReviewImageRepository shopReviewImageRepository;
    private final ShopReviewMenuRepository shopReviewMenuRepository;
    private final Clock clock;

    public ShopReviewFixture(
        ShopReviewRepository shopReviewRepository,
        ShopReviewImageRepository shopReviewImageRepository,
        ShopReviewMenuRepository shopReviewMenuRepository,
        Clock clock
    ) {
        this.shopReviewRepository = shopReviewRepository;
        this.shopReviewImageRepository = shopReviewImageRepository;
        this.shopReviewMenuRepository = shopReviewMenuRepository;
        this.clock = clock;
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

    @FixedDate(year = 2024, month = 8, day = 7)
    public ShopReview 최신_리뷰_2024_08_07(Student student, Shop shop) {
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

    public ShopReview 리뷰_5점(Student student, Shop shop) {
        ShopReview shopReview = ShopReview.builder()
            .reviewer(student)
            .content("여기 진짜 맛있어요.")
            .rating(5)
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
