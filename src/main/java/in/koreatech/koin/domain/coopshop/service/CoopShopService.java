package in.koreatech.koin.domain.coopshop.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.dto.CoopShopsResponse;
import in.koreatech.koin.domain.coopshop.exception.CoopShopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.model.DayType;
import in.koreatech.koin.domain.coopshop.repository.CoopOpenRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopSemesterRepository;
import in.koreatech.koin.domain.dining.model.DiningType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoopShopService {

    private final Clock clock;
    private final CoopShopRepository coopShopRepository;
    private final CoopOpenRepository coopOpenRepository;
    private final CoopShopSemesterRepository coopShopSemesterRepository;

    public CoopShopsResponse getCoopShops() {
        CoopShopSemester coopShopSemester = coopShopSemesterRepository.getByIsApplied(true);
        return CoopShopsResponse.from(coopShopSemester);
    }

    public CoopShopResponse getCoopShopByName(String coopShopName) {
        CoopShopSemester coopShopSemester = coopShopSemesterRepository.getByIsApplied(true);
        CoopShop coopShop = coopShopRepository.getByCoopShopSemesterAndName(coopShopSemester,
            CoopShopType.from(coopShopName));
        return CoopShopResponse.from(coopShop);
    }

    public CoopShopResponse getCoopShop(Integer id) {
        CoopShop coopShop = coopShopRepository.getById(id);
        return CoopShopResponse.from(coopShop);
    }

    public boolean getIsOpened(LocalDateTime now, CoopShopType coopShopType, DiningType type, Boolean isMinus) {
        try {
            DayType todayType =
                (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY)
                    ? DayType.WEEKEND : DayType.WEEKDAYS;
            CoopShop coopShop = coopShopRepository.getByName(coopShopType);
            CoopOpen open = coopOpenRepository
                .getByCoopShopAndTypeAndDayOfWeek(coopShop, type.getDiningName(), todayType);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime openTime = LocalTime.parse(open.getOpenTime(), formatter)
                .atDate(now.toLocalDate());

            if (isMinus) {
                openTime = openTime.minusHours(1);
            }

            LocalDateTime closeTime = LocalTime.parse(open.getCloseTime(), formatter).atDate(now.toLocalDate());

            return !(now.isBefore(openTime) || now.isAfter(closeTime));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Transactional
    public void updateSemester() {
        CoopShopSemester currentSemester = coopShopSemesterRepository.getByIsApplied(true);
        if (validateSemester(currentSemester)) {
            return;
        }

        currentSemester.updateApply(false);
        CoopShopSemester nextSemester = coopShopSemesterRepository.getTopByOrderByToDateDesc();
        if (!validateSemester(nextSemester)) {
            throw CoopShopSemesterNotFoundException.withDetail("");
        }
        nextSemester.updateApply(true);
    }

    public boolean validateSemester(CoopShopSemester coopShopSemester) {
        LocalDate today = LocalDate.now(clock);
        return today.isAfter(coopShopSemester.getFromDate()) && today.isBefore(coopShopSemester.getToDate());
    }
}
