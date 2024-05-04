package in.koreatech.koin.admin.land.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.land.dto.AdminLandsRequest;
import in.koreatech.koin.admin.land.dto.AdminLandsResponse;
import in.koreatech.koin.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.global.model.Criteria;
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
    public void createLands(AdminLandsRequest adminLandsRequest) {
        Land existingLand = adminLandRepository.findByName(adminLandsRequest.name());
        if (existingLand != null) {
            throw new Exception("Land name is already in use.");
        }
        Land newLand = Land.builder()
            .name(adminLandsRequest.name())
            .size(adminLandsRequest.size())
            .roomType(adminLandsRequest.roomType())
            .latitude(adminLandsRequest.latitude())
            .longitude(adminLandsRequest.longitude())
            .phone(adminLandsRequest.phone())
            .imageUrls(adminLandsRequest.imageUrls())
            .address(adminLandsRequest.address())
            .description(adminLandsRequest.description())
            .floor(adminLandsRequest.floor())
            .deposit(adminLandsRequest.deposit())
            .monthlyFee(adminLandsRequest.monthlyFee())
            .charterFee(adminLandsRequest.charterFee())
            .managementFee(adminLandsRequest.managementFee())
            .optRefrigerator(adminLandsRequest.optRefrigerator())
            .optCloset(adminLandsRequest.optCloset())
            .optTv(adminLandsRequest.optTv())
            .optMicrowave(adminLandsRequest.optMicrowave())
            .optGasRange(adminLandsRequest.optGasRange())
            .optInduction(adminLandsRequest.optInduction())
            .optWaterPurifier(adminLandsRequest.optWaterPurifier())
            .optAirConditioner(adminLandsRequest.optAirConditioner())
            .optWasher(adminLandsRequest.optWasher())
            .optBed(adminLandsRequest.optBed())
            .optDesk(adminLandsRequest.optDesk())
            .optShoeCloset(adminLandsRequest.optShoeCloset())
            .optElectronicDoorLocks(adminLandsRequest.optElectronicDoorLocks())
            .optBidet(adminLandsRequest.optBidet())
            .optVeranda(adminLandsRequest.optVeranda())
            .optElevator(adminLandsRequest.optElevator())
            .build();

        adminLandRepository.save(newLand);

    }
}
