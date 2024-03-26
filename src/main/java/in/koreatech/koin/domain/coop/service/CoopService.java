package in.koreatech.koin.domain.coop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoopService {

    private final DiningRepository diningRepository;

    @Transactional
    public void changeSoldOut(SoldOutRequest soldOutRequest) {
        diningRepository.updateSoldOut(soldOutRequest.menuId(), soldOutRequest.soldOut());
    }
}
