package in.koreatech.koin.domain.coopshop.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.repository.CoopOpenRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.dining.model.DiningType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoopShopService {

    private final CoopShopRepository coopShopRepository;
    private final CoopOpenRepository coopOpenRepository;

    public List<CoopShopResponse> getCoopShops() {
        return coopShopRepository.findAllByIsDeletedFalse().stream()
            .map(CoopShopResponse::from)
            .toList();
    }

    public CoopShopResponse getCoopShop(Integer id) {
        CoopShop coopShop = coopShopRepository.getById(id);
        return CoopShopResponse.from(coopShop);
    }

    public boolean getIsOpened(LocalDateTime now, CoopShopType coopShopType, DiningType type, Boolean isMinus) {
        try {
            String todayType =
                (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) ? "주말" : "평일";
            CoopShop coopShop = coopShopRepository.getByName(coopShopType.getName());
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
}
