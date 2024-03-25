package in.koreatech.koin.domain.dining.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dining.dto.SoldOutRequest;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoopDiningService {

    private final DiningRepository diningRepository;

    @Transactional
    public void changeSoldOut(SoldOutRequest soldOutRequest) {
        diningRepository.updateSoldOut(soldOutRequest.menuId(), soldOutRequest.soldOut());
    }
}
