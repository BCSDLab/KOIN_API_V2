package in.koreatech.koin.admin.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.banner.dto.response.AdminBannerResponse;
import in.koreatech.koin.admin.banner.repository.AdminBannerRepository;
import in.koreatech.koin.domain.banner.model.Banner;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminBannerService {

    private final AdminBannerRepository adminBannerRepository;

    public AdminBannerResponse getBanner(Integer bannerId) {
        Banner banner = adminBannerRepository.getById(bannerId);
        return AdminBannerResponse.from(banner);
    }

}
