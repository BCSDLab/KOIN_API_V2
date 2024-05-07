package in.koreatech.koin.fixture;

import static in.koreatech.koin.domain.dining.model.DiningType.DINNER;
import static in.koreatech.koin.domain.dining.model.DiningType.LUNCH;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class DiningFixture {

    private final DiningRepository diningRepository;

    public DiningFixture(DiningRepository diningRepository) {
        this.diningRepository = diningRepository;
    }

    public Dining 능수관_점심(LocalDate date) {
        return diningRepository.save(
            Dining.builder()
                .date(date)
                .type(LUNCH)
                .place("능수관")
                .priceCard(6000)
                .priceCash(6000)
                .kcal(300)
                .menu("""
                    ["참치김치볶음밥", "유부된장국", "땡초부추전", "누룽지탕"]""")
                .build()
        );
    }

    public Dining 캠퍼스2_점심(LocalDate date) {
        return diningRepository.save(
            Dining.builder()
                .date(date)
                .type(LUNCH)
                .place("2캠퍼스")
                .priceCard(6000)
                .priceCash(6000)
                .kcal(881)
                .menu("""
                    ["혼합잡곡밥", "가쓰오장국", "땡초부추전", "누룽지탕"]""")
                .build()
        );
    }

    public Dining A코스_점심(LocalDate date) {
        return diningRepository.save(
            Dining.builder()
                .date(date)
                .type(LUNCH)
                .place("A코스")
                .priceCard(6000)
                .priceCash(6000)
                .kcal(881)
                .menu("""
                    ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]""")
                .build()
        );
    }

    public Dining A코스_저녁(LocalDate date) {
        return diningRepository.save(
            Dining.builder()
                .date(date)
                .type(DINNER)
                .place("A코스")
                .priceCard(6000)
                .priceCash(6000)
                .kcal(881)
                .menu("""
                    ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]""")
                .build()
        );
    }

    public Dining B코스_점심(LocalDate date) {
        return diningRepository.save(
            Dining.builder()
                .date(date)
                .type(LUNCH)
                .place("B코스")
                .priceCard(6000)
                .priceCash(6000)
                .kcal(881)
                .menu("""
                    ["병아리", "소고기", "땡초", "탕"]""")
                .build()
        );
    }
}
