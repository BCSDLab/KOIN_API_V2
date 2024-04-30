package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.domain.land.repository.LandRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class LandFixture {

    private final LandRepository landRepository;

    public LandFixture(LandRepository landRepository) {
        this.landRepository = landRepository;
    }

    public Land 신안빌() {
        return landRepository.save(
            Land.builder()
                .internalName("신")
                .name("신안빌")
                .roomType("원룸")
                .latitude("37.555")
                .longitude("126.555")
                .floor(1)
                .monthlyFee("100")
                .charterFee("1000")
                .deposit("1000")
                .managementFee("100")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .size("100.0")
                .imageUrls("""
                    ["https://example1.test.com/image.jpeg",
                    "https://example2.test.com/image.jpeg"]
                    """)
                .build()
        );
    }

    public Land 에듀윌() {
        return landRepository.save(
            Land.builder()
                .internalName("에")
                .name("에듀윌")
                .roomType("원룸")
                .latitude("37.555")
                .longitude("126.555")
                .floor(1)
                .monthlyFee("100")
                .charterFee("1000")
                .deposit("1000")
                .managementFee("100")
                .phone("010-1133-5555")
                .address("천안시 동남구 강남구")
                .size("100.0")
                .imageUrls("""
                    ["https://example1.test.com/image.jpeg",
                    "https://example2.test.com/image.jpeg"]
                    """)
                .build()
        );
    }
}
