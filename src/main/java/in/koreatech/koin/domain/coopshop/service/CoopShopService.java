package in.koreatech.koin.domain.coopshop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoopShopService {

    private final CoopShopRepository coopShopRepository;

    public List<CoopShopResponse> getCoopShops() {
        return coopShopRepository.findAllByIsDeletedFalse().stream()
            .map(CoopShopResponse::from)
            .toList();
    }

    public CoopShopResponse getCoopShop(Integer id) {
        CoopShop coopShop = coopShopRepository.getById(id);
        return CoopShopResponse.from(coopShop);
    }
}
