package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.benefit.repository.BenefitCategoryMapRepository;
import in.koreatech.koin.domain.benefit.repository.BenefitCategoryRepository;

@Component
public class BenefitCategoryFixture {

    private final BenefitCategoryRepository benefitCategoryRepository;
    private final BenefitCategoryMapRepository benefitCategoryMapRepository;

    public BenefitCategoryFixture(
        BenefitCategoryRepository benefitCategoryRepository,
        BenefitCategoryMapRepository benefitCategoryMapRepository
    ) {
        this.benefitCategoryRepository = benefitCategoryRepository;
        this.benefitCategoryMapRepository = benefitCategoryMapRepository;
    }

    public BenefitCategory 배달비_무료() {
        return benefitCategoryRepository.save(BenefitCategory.builder()
            .title("배달비 아끼기")
            .detail("계좌이체하면 배달비가 무료(할인)인 상점들을 모아뒀어요.")
            .onImageUrl("https://stage-static.koreatech.in/shop/benefit/deliveryOn.png")
            .offImageUrl("https://stage-static.koreatech.in/shop/benefit/deliveryOff.png")
            .build()
        );
    }

    public BenefitCategory 최소주문금액_무료() {
        return benefitCategoryRepository.save(BenefitCategory.builder()
            .title("최소주문금액 무료")
            .detail("계좌이체하면 최소주문금액이 무료인 상점들을 모아뒀어요.")
            .onImageUrl("https://stage-static.koreatech.in/shop/benefit/paidOn.png")
            .offImageUrl("https://stage-static.koreatech.in/shop/benefit/paidOff.png")
            .build()
        );
    }

    public BenefitCategory 서비스_증정() {
        return benefitCategoryRepository.save(BenefitCategory.builder()
            .title("서비스 증정")
            .detail("계좌이체하면 서비스를 주는 상점들을 모아뒀어요.")
            .onImageUrl("https://stage-static.koreatech.in/shop/benefit/serviceOn.png")
            .offImageUrl("https://stage-static.koreatech.in/shop/benefit/serviceOff.png")
            .build()
        );
    }

    public BenefitCategory 가게까지_픽업() {
        return benefitCategoryRepository.save(BenefitCategory.builder()
            .title("가게까지 픽업")
            .detail("사장님께서 직접 가게까지 데려다주시는 상점들을 모아뒀어요.")
            .onImageUrl("https://stage-static.koreatech.in/shop/benefit/pickUpOn.png")
            .offImageUrl("https://stage-static.koreatech.in/shop/benefit/pickUpOff.png")
            .build()
        );
    }
}
