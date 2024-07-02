package in.koreatech.koin.fixture;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class CoopShopFixture {

    private final CoopShopRepository coopShopRepository;

    public CoopShopFixture(CoopShopRepository coopShopRepository) {
        this.coopShopRepository = coopShopRepository;
    }

    public CoopShop 학생식당() {
        var coopShop = coopShopRepository.save(
            CoopShop.builder()
                .name("학생식당")
                .location("학생회관 1층")
                .phone("041-000-0000")
                .remarks("공휴일 휴무")
                .build()
        );
        coopShop.getCoopOpens().addAll(
            List.of(
                CoopOpen.builder()
                    .openTime("08:00")
                    .closeTime("09:00")
                    .coopShop(coopShop)
                    .dayOfWeek("평일")
                    .type("아침")
                    .build(),
                CoopOpen.builder()
                    .openTime("11:30")
                    .closeTime("13:30")
                    .coopShop(coopShop)
                    .dayOfWeek("평일")
                    .type("점심")
                    .build(),
                CoopOpen.builder()
                    .openTime("17:30")
                    .closeTime("18:30")
                    .coopShop(coopShop)
                    .dayOfWeek("평일")
                    .type("저녁")
                    .build(),
                CoopOpen.builder()
                    .openTime("11:30")
                    .closeTime("13:00")
                    .coopShop(coopShop)
                    .dayOfWeek("주말")
                    .type("점심")
                    .build()
            )
        );
        return coopShopRepository.save(coopShop);
    }

    public CoopShop 세탁소() {
        var coopShop = coopShopRepository.save(
            CoopShop.builder()
                .name("세탁소")
                .location("학생회관 2층")
                .phone("041-000-0000")
                .remarks("연중무휴")
                .build()
        );
        coopShop.getCoopOpens().addAll(
            List.of(
                CoopOpen.builder()
                    .openTime("09:00")
                    .closeTime("18:00")
                    .coopShop(coopShop)
                    .dayOfWeek("평일")
                    .build(),
                CoopOpen.builder()
                    .openTime("미운영")
                    .closeTime("미운영")
                    .coopShop(coopShop)
                    .dayOfWeek("주말")
                    .build()
            )
        );
        return coopShopRepository.save(coopShop);
    }
}
