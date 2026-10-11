package in.koreatech.koin.domain.coopshop.service;

import static in.koreatech.koin.domain.dining.model.DiningType.*;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.dto.CoopShopsResponse;
import in.koreatech.koin.domain.coopshop.exception.CoopOpenNotFoundException;
import in.koreatech.koin.domain.coopshop.exception.CoopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.exception.DiningTypeNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopName;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.model.DayType;
import in.koreatech.koin.domain.coopshop.repository.CoopNameRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopOpenRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopSemesterRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.dining.model.DiningType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoopShopService {

    private final Clock clock;
    private final CoopShopRepository coopShopRepository;
    private final CoopOpenRepository coopOpenRepository;
    private final CoopSemesterRepository coopSemesterRepository;
    private final CoopNameRepository coopNameRepository;

    public CoopShopsResponse getCoopShops() {
        CoopSemester coopSemester = coopSemesterRepository.getByIsApplied(true);
        return CoopShopsResponse.from(coopSemester);
    }

    public CoopShopResponse getCoopShop(Integer coopNameId) {
        CoopSemester currentSemester = coopSemesterRepository.getByIsApplied(true);
        CoopShop coopShop = coopShopRepository.getByCoopNameIdAndCoopSemester(coopNameId, currentSemester);
        return CoopShopResponse.from(coopShop);
    }

    public boolean getIsOpened(LocalDateTime now, CoopShopType coopShopType, DiningType type, Boolean isMinus) {
        try {
            CoopSemester currentSemester = coopSemesterRepository.getByIsApplied(true);
            CoopName coopName = coopNameRepository.getByName(coopShopType);
            CoopShop coopShop = coopShopRepository.getByCoopNameIdAndCoopSemester(coopName.getId(), currentSemester);
            CoopOpen open = findOpen(coopShop, type.getDiningName(), now.getDayOfWeek());

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

    private CoopOpen findOpen(CoopShop coopShop, String type, DayOfWeek dayOfWeek) {
        for (DayType dayType : candidateDayTypes(dayOfWeek)) {
            Optional<CoopOpen> open = coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(coopShop, type, dayType);
            if (open.isPresent()) {
                return open.get();
            }
        }
        throw CoopOpenNotFoundException.withDetail(
            String.format("coopShop: %s, type: %s, day of week: %s", coopShop, type, dayOfWeek)
        );
    }

    private List<DayType> candidateDayTypes(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case SATURDAY -> List.of(DayType.SATURDAY, DayType.WEEKEND);
            case SUNDAY -> List.of(DayType.SUNDAY, DayType.WEEKEND);
            default -> List.of(DayType.WEEKDAYS);
        };
    }

    public DiningType getDiningType() {
        if (LocalTime.now(clock).isAfter(BREAKFAST.getStartTime().minusHours(1))
            && LocalTime.now(clock).isBefore(BREAKFAST.getEndTime())) {
            return BREAKFAST;
        }
        if (LocalTime.now(clock).isAfter(LUNCH.getStartTime().minusHours(1))
            && LocalTime.now(clock).isBefore(LUNCH.getEndTime())) {
            return LUNCH;
        }
        if (LocalTime.now(clock).isAfter(DINNER.getStartTime().minusHours(1))
            && LocalTime.now(clock).isBefore(DINNER.getEndTime())) {
            return DINNER;
        }

        throw DiningTypeNotFoundException.withDetail(LocalTime.now() + "");
    }

    @Transactional
    public void updateSemester() {
        CoopSemester currentSemester = coopSemesterRepository.getByIsApplied(true);
        if (validateSemester(currentSemester)) {
            return;
        }

        currentSemester.updateApply(false);
        CoopSemester nextSemester = coopSemesterRepository.getTopByOrderByToDateDesc();
        if (!validateSemester(nextSemester)) {
            throw CoopSemesterNotFoundException.withDetail("");
        }
        nextSemester.updateApply(true);
    }

    public boolean validateSemester(CoopSemester coopSemester) {
        LocalDate today = LocalDate.now(clock);
        return today.isAfter(coopSemester.getFromDate()) && today.isBefore(coopSemester.getToDate());
    }
}
