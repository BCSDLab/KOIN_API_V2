package in.koreatech.koin.domain.coopshop.service;

import static in.koreatech.koin.domain.dining.model.DiningType.*;

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
import in.koreatech.koin.domain.coopshop.exception.CoopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.exception.DiningTypeNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.model.DayType;
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

    public CoopShopsResponse getCoopShops() {
        CoopSemester coopSemester = coopSemesterRepository.getByIsApplied(true);
        return CoopShopsResponse.from(coopSemester);
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
            CoopSemester semester = coopSemesterRepository.getByIsApplied(true);
            CoopShop coopShop = coopShopRepository.getByNameAndCoopSemesterId(coopShopType, semester.getId());
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

    public DiningType getDiningType(){
        if(LocalTime.now(clock).isAfter(BREAKFAST.getStartTime().minusHours(1))
            && LocalTime.now(clock).isBefore(BREAKFAST.getEndTime())){
            return BREAKFAST;
        }
        if(LocalTime.now(clock).isAfter(LUNCH.getStartTime().minusHours(1))
            && LocalTime.now(clock).isBefore(LUNCH.getEndTime())){
            return LUNCH;
        }
        if(LocalTime.now(clock).isAfter(DINNER.getStartTime().minusHours(1))
            && LocalTime.now(clock).isBefore(DINNER.getEndTime())){
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
