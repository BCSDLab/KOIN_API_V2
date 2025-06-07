package in.koreatech.koin.admin.owner.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.owner.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnerUpdateRequest;
import in.koreatech.koin.admin.owner.dto.AdminOwnerUpdateResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnersResponse;
import in.koreatech.koin.admin.owner.dto.OwnersCondition;
import in.koreatech.koin.admin.owner.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.owner.repository.AdminOwnerShopRedisRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerIncludingShop;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin._common.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOwnerService {

    private final AdminOwnerRepository adminOwnerRepository;
    private final AdminOwnerShopRedisRepository adminOwnerShopRedisRepository;
    private final AdminShopRepository adminShopRepository;
    private final AdminUserRepository adminUserRepository;

    @Transactional
    public void allowOwnerPermission(Integer id) {
        Owner owner = adminOwnerRepository.getById(id);
        owner.getUser().permitAuth();
        adminOwnerShopRedisRepository.findById(id).ifPresent(ownerShop -> {
            Integer shopId = ownerShop.getShopId();
            if (shopId != null) {
                Shop shop = adminShopRepository.getById(shopId);
                shop.updateOwner(owner);
                owner.setGrantShop(true);
            }
        });
    }

    public AdminOwnerResponse getOwner(Integer ownerId) {
        Owner owner = adminOwnerRepository.getById(ownerId);

        List<Integer> shopsId = adminShopRepository.findAllByOwnerId(ownerId)
            .stream()
            .map(Shop::getId)
            .toList();

        return AdminOwnerResponse.of(owner, shopsId);
    }

    @Transactional
    public AdminOwnerUpdateResponse updateOwner(Integer ownerId, AdminOwnerUpdateRequest request) {
        Owner owner = adminOwnerRepository.getById(ownerId);
        owner.update(request);
        return AdminOwnerUpdateResponse.from(owner);
    }

    public AdminNewOwnersResponse getNewOwners(OwnersCondition ownersCondition) {
        ownersCondition.checkDataConstraintViolation();

        Integer totalOwners = adminUserRepository.findUsersCountByUserTypeAndIsAuthed(UserType.OWNER, false);
        Criteria criteria = Criteria.of(ownersCondition.page(), ownersCondition.limit(), totalOwners);
        Sort.Direction direction = ownersCondition.getDirection();

        Page<Owner> result = getNewOwnersResultPage(ownersCondition, criteria, direction);

        List<OwnerIncludingShop> ownerIncludingShops = new ArrayList<>();
        for (Owner owner : result.getContent()) {
            Shop shop = adminOwnerShopRedisRepository.findById(owner.getId())
                .map(ownerShop -> {
                    Integer shopId = ownerShop.getShopId();
                    return shopId != null ? adminShopRepository.findById(shopId).orElse(null) : null;
                })
                .orElse(null);

            OwnerIncludingShop ownerIncludingShop = OwnerIncludingShop.of(owner, shop);
            ownerIncludingShops.add(ownerIncludingShop);
        }

        return AdminNewOwnersResponse.of(ownerIncludingShops, result, criteria);
    }

    public AdminOwnersResponse getOwners(OwnersCondition ownersCondition) {
        ownersCondition.checkDataConstraintViolation();

        Integer totalOwners = adminUserRepository.findUsersCountByUserTypeAndIsAuthed(UserType.OWNER, true);
        Criteria criteria = Criteria.of(ownersCondition.page(), ownersCondition.limit(), totalOwners);
        Sort.Direction direction = ownersCondition.getDirection();

        Page<Owner> result = getOwnersResultPage(ownersCondition, criteria, direction);

        return AdminOwnersResponse.of(result, criteria);
    }

    private Page<Owner> getNewOwnersResultPage(OwnersCondition ownersCondition, Criteria criteria,
        Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(direction, "user.createdAt")
        );

        if (ownersCondition.searchType() == OwnersCondition.SearchType.EMAIL) {
            return adminOwnerRepository.findPageUnauthenticatedOwnersByEmail(ownersCondition.query(), pageRequest);
        }
        if (ownersCondition.searchType() == OwnersCondition.SearchType.NAME) {
            return adminOwnerRepository.findPageUnauthenticatedOwnersByName(ownersCondition.query(), pageRequest);
        }
        return adminOwnerRepository.findPageUnauthenticatedOwners(pageRequest);
    }

    private Page<Owner> getOwnersResultPage(OwnersCondition ownersCondition, Criteria criteria,
        Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(direction, "user.createdAt")
        );

        if (ownersCondition.searchType() == OwnersCondition.SearchType.EMAIL) {
            return adminOwnerRepository.findPageOwnersByEmail(ownersCondition.query(), pageRequest);
        }
        if (ownersCondition.searchType() == OwnersCondition.SearchType.NAME) {
            return adminOwnerRepository.findPageOwnersByName(ownersCondition.query(), pageRequest);
        }
        return adminOwnerRepository.findPageOwners(pageRequest);
    }
}
