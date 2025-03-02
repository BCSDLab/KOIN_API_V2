package in.koreatech.koin.admin.land.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.land.dto.AdminLandResponse;
import in.koreatech.koin.admin.land.dto.AdminLandRequest;
import in.koreatech.koin.admin.land.dto.AdminLandsResponse;
import in.koreatech.koin.admin.land.execption.LandNameDuplicationException;
import in.koreatech.koin.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin._common.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLandService {

    private final AdminLandRepository adminLandRepository;

    public AdminLandsResponse getLands(Integer page, Integer limit, Boolean isDeleted) {

        // page > totalPage인 경우 totalPage로 조회하기 위해
        Integer total = adminLandRepository.countAllByIsDeleted(isDeleted);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<Land> result = adminLandRepository.findAllByIsDeleted(isDeleted, pageRequest);

        return AdminLandsResponse.of(result, criteria);
    }

    @Transactional
    public void createLands(AdminLandRequest adminLandRequest) {
        if (adminLandRepository.findByName(adminLandRequest.name()).isPresent()) {
            throw LandNameDuplicationException.withDetail("name: " + adminLandRequest.name());
        }
        Land land = adminLandRequest.toLand();
        adminLandRepository.save(land);
    }

    @Transactional
    public void deleteLand(Integer id) {
        Land land = adminLandRepository.getById(id);
        land.delete();
    }

    public AdminLandResponse getLand(Integer id) {
        Land land = adminLandRepository.getById(id);
        return AdminLandResponse.from(land);
    }

    @Transactional
    public void updateLand(Integer id, AdminLandRequest request) {
        Land land = adminLandRepository.getById(id);
        land.update(
            request.internalName(),
            request.name(),
            request.size(),
            request.roomType(),
            request.latitude(),
            request.longitude(),
            request.phone(),
            request.imageUrls(),
            request.address(),
            request.description(),
            request.floor(),
            request.deposit(),
            request.monthlyFee(),
            request.charterFee(),
            request.managementFee(),
            request.optRefrigerator(),
            request.optCloset(),
            request.optTv(),
            request.optMicrowave(),
            request.optGasRange(),
            request.optInduction(),
            request.optWaterPurifier(),
            request.optAirConditioner(),
            request.optWasher(),
            request.optBed(),
            request.optDesk(),
            request.optShoeCloset(),
            request.optElectronicDoorLocks(),
            request.optBidet(),
            request.optVeranda(),
            request.optElevator()
        );

    }

    @Transactional
    public void undeleteLand(Integer id) {
        Land land = adminLandRepository.getById(id);
        land.undelete();
    }
}
