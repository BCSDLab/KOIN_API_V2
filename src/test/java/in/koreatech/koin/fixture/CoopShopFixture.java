package in.koreatech.koin.fixture;

import static in.koreatech.koin.domain.coopshop.model.CoopShopType.CAFETERIA;
import static in.koreatech.koin.domain.coopshop.model.CoopShopType.LAUNDRY;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.DayType;
import in.koreatech.koin.domain.coopshop.repository.CoopSemesterRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class CoopShopFixture {

    private final CoopShopRepository coopShopRepository;
    private final CoopSemesterRepository coopSemesterRepository;

    public CoopShopFixture(
        CoopShopRepository coopShopRepository,
        CoopSemesterRepository coopSemesterRepository
    ) {
        this.coopShopRepository = coopShopRepository;
        this.coopSemesterRepository = coopSemesterRepository;
    }

    public CoopSemester _23_2학기() {
        var coopShopSemester = coopSemesterRepository.save(
            CoopSemester.builder()
                .semester("23-2학기")
                .fromDate(LocalDate.of(2023, 9, 2))
                .toDate(LocalDate.of(2023, 12, 20))
                .build()
        );
        coopShopSemester.updateApply(true);

        var cafeteria = CoopShop.builder()
            .name(CAFETERIA.getCoopShopName())
            .location("학생회관 1층")
            .phone("041-000-0000")
            .remarks("공휴일 휴무")
            .coopSemester(coopShopSemester)
            .build();

        cafeteria.getCoopOpens().addAll(
            List.of(
                CoopOpen.builder()
                    .openTime("08:00")
                    .closeTime("09:00")
                    .coopShop(cafeteria)
                    .dayOfWeek(DayType.WEEKDAYS)
                    .type("아침")
                    .build()
            )
        );

        var laundry = CoopShop.builder()
            .name(LAUNDRY.getCoopShopName())
            .location("학생회관 2층")
            .phone("041-000-0000")
            .remarks("연중무휴")
            .coopSemester(coopShopSemester)
            .build();

        coopShopRepository.save(cafeteria);
        coopShopRepository.save(laundry);
        return coopSemesterRepository.save(coopShopSemester);
    }

    public CoopSemester _23_겨울학기() {
        var coopShopSemester = coopSemesterRepository.save(
            CoopSemester.builder()
                .semester("23-겨울학기")
                .fromDate(LocalDate.of(2023, 12, 21))
                .toDate(LocalDate.of(2024, 2, 28))
                .build()
        );

        var coopShop = CoopShop.builder()
            .name(LAUNDRY.getCoopShopName())
            .location("학생회관 2층")
            .phone("041-000-0000")
            .remarks("연중무휴")
            .coopSemester(coopShopSemester)
            .build();

        coopShop.getCoopOpens().addAll(
            List.of(
                CoopOpen.builder()
                    .openTime("09:00")
                    .closeTime("18:00")
                    .coopShop(coopShop)
                    .dayOfWeek(DayType.WEEKDAYS)
                    .build()
            )
        );

        coopShopSemester.getCoopShops().addAll(List.of(coopShop));
        return coopSemesterRepository.save(coopShopSemester);
    }

    public CoopSemester 현재학기() {
        var coopShopSemester = coopSemesterRepository.save(
            CoopSemester.builder()
                .semester("현재 학기")
                .fromDate(LocalDate.of(2023, 12, 21))
                .toDate(LocalDate.of(2024, 2, 28))
                .build()
        );

        coopShopSemester.updateApply(true);

        var coopShop = CoopShop.builder()
            .name(CAFETERIA.getCoopShopName())
            .location("학생회관 2층")
            .phone("041-000-0000")
            .remarks("연중무휴")
            .coopSemester(coopShopSemester)
            .build();

        coopShop.getCoopOpens().addAll(
            List.of(
                CoopOpen.builder()
                    .type("아침")
                    .openTime("08:30")
                    .closeTime("09:30")
                    .coopShop(coopShop)
                    .dayOfWeek(DayType.WEEKDAYS)
                    .build(),
                CoopOpen.builder()
                    .type("점심")
                    .openTime("11:30")
                    .closeTime("13:30")
                    .coopShop(coopShop)
                    .dayOfWeek(DayType.WEEKDAYS)
                    .build(),
                CoopOpen.builder()
                    .type("저녁")
                    .openTime("17:30")
                    .closeTime("18:30")
                    .coopShop(coopShop)
                    .dayOfWeek(DayType.WEEKDAYS)
                    .build(),
                CoopOpen.builder()
                    .type("아침")
                    .openTime("미운영")
                    .closeTime("미운영")
                    .coopShop(coopShop)
                    .dayOfWeek(DayType.WEEKEND)
                    .build(),
                CoopOpen.builder()
                    .type("점심")
                    .openTime("11:30")
                    .closeTime("13:30")
                    .coopShop(coopShop)
                    .dayOfWeek(DayType.WEEKEND)
                    .build(),
                CoopOpen.builder()
                    .type("저녁")
                    .openTime("17:30")
                    .closeTime("18:30")
                    .coopShop(coopShop)
                    .dayOfWeek(DayType.WEEKEND)
                    .build()
            )
        );

        coopShopSemester.getCoopShops().addAll(List.of(coopShop));
        return coopSemesterRepository.save(coopShopSemester);
    }
}
