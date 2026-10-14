package in.koreatech.koin.domain.coopshop.service;

import static in.koreatech.koin.domain.dining.model.DiningType.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.dto.CoopShopsResponse;
import in.koreatech.koin.domain.coopshop.exception.CoopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.exception.DiningTypeNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopName;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.repository.CoopNameRepository;
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
        CoopSemester currentSemester = coopSemesterRepository.getByIsApplied(true);
        CoopName coopName = coopNameRepository.getByName(coopShopType);
        CoopShop coopShop = coopShopRepository.getByCoopNameIdAndCoopSemester(coopName.getId(), currentSemester);
        return coopShop.getOpensOn(now.getDayOfWeek()).stream()
            .filter(open -> type.getDiningName().equals(open.getType()))
            .anyMatch(open -> isOpenAt(open, now, isMinus));
    }

    private boolean isOpenAt(CoopOpen open, LocalDateTime now, Boolean isMinus) {
        if (open.getOpenTime() == null || open.getCloseTime() == null) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime openTime = LocalTime.parse(open.getOpenTime(), formatter)
                .atDate(now.toLocalDate());

            if (Boolean.TRUE.equals(isMinus)) {
                openTime = openTime.minusHours(1);
            }

            LocalDateTime closeTime = LocalTime.parse(open.getCloseTime(), formatter).atDate(now.toLocalDate());

            return !(now.isBefore(openTime) || now.isAfter(closeTime));
        } catch (DateTimeParseException e) {
            return false;
        }
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
        return !today.isBefore(coopSemester.getFromDate()) && !today.isAfter(coopSemester.getToDate())
            && !coopSemester.getCoopShops().isEmpty();
    }
}
