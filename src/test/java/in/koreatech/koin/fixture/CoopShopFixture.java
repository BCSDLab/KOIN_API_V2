package in.koreatech.koin.fixture;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopSemesterRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class CoopShopFixture {

    private final CoopShopRepository coopShopRepository;
    private final CoopShopSemesterRepository coopShopSemesterRepository;

    public CoopShopFixture(
        CoopShopRepository coopShopRepository,
        CoopShopSemesterRepository coopShopSemesterRepository
    ) {
        this.coopShopRepository = coopShopRepository;
        this.coopShopSemesterRepository = coopShopSemesterRepository;
    }

    public CoopShopSemester _23_2학기() {
        var coopShopSemester = coopShopSemesterRepository.save(
            CoopShopSemester.builder()
                .semester("23-2학기")
                .fromDate(LocalDate.of(2023, 9, 2))
                .toDate(LocalDate.of(2023, 12, 20))
                .build()
        );
        coopShopSemester.updateApply(true);

        var cafeteria = CoopShop.builder()
            .name(CoopShopType.CAFETERIA)
            .location("학생회관 1층")
            .phone("041-000-0000")
            .remarks("공휴일 휴무")
            .coopShopSemester(coopShopSemester)
            .build();

        cafeteria.getCoopOpens().addAll(
            List.of(
                CoopOpen.builder()
                    .openTime("08:00")
                    .closeTime("09:00")
                    .coopShop(cafeteria)
                    .dayOfWeek("평일")
                    .type("아침")
                    .build()
            )
        );

        var laundry = CoopShop.builder()
            .name(CoopShopType.LAUNDRY)
            .location("학생회관 2층")
            .phone("041-000-0000")
            .remarks("연중무휴")
            .coopShopSemester(coopShopSemester)
            .build();

        coopShopRepository.save(cafeteria);
        coopShopRepository.save(laundry);
        return coopShopSemesterRepository.save(coopShopSemester);
    }

    public CoopShopSemester _23_겨울학기() {
        var coopShopSemester = coopShopSemesterRepository.save(
            CoopShopSemester.builder()
                .semester("23-겨울학기")
                .fromDate(LocalDate.of(2023, 12, 21))
                .toDate(LocalDate.of(2024, 2, 28))
                .build()
        );

        var coopShop = CoopShop.builder()
            .name(CoopShopType.LAUNDRY)
            .location("학생회관 2층")
            .phone("041-000-0000")
            .remarks("연중무휴")
            .coopShopSemester(coopShopSemester)
            .build();

        coopShop.getCoopOpens().addAll(
            List.of(
                CoopOpen.builder()
                    .openTime("09:00")
                    .closeTime("18:00")
                    .coopShop(coopShop)
                    .dayOfWeek("평일")
                    .build()
            )
        );

        coopShopSemester.getCoopShops().addAll(List.of(coopShop));
        return coopShopSemesterRepository.save(coopShopSemester);
    }
}
