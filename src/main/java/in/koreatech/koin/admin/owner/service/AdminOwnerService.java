package in.koreatech.koin.admin.owner.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.owner.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.owner.repository.AdminOwnerShopRedisRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.admin.user.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnersResponse;
import in.koreatech.koin.admin.user.dto.OwnersCondition;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerIncludingShop;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.global.model.Criteria;
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
        owner.getUser().auth();
        Optional<OwnerShop> ownerShop = adminOwnerShopRedisRepository.findById(id);
        if (ownerShop.isPresent()) {
            Integer shopId = ownerShop.get().getShopId();
            if (shopId != null) {
                Shop shop = adminShopRepository.getById(shopId);
                shop.updateOwner(owner);
                owner.setGrantShop(true);
            }
            adminOwnerShopRedisRepository.deleteById(id);
        }
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

        List<OwnerIncludingShop> ownerIncludingShops = result.getContent().stream()
            .map(owner -> {
                Optional<OwnerShop> ownerShop = adminOwnerShopRedisRepository.findById(owner.getId());
                return ownerShop
                    .map(os -> {
                        Shop shop = adminShopRepository.findById(os.getShopId()).orElse(null);
                        return OwnerIncludingShop.of(owner, shop);
                    })
                    .orElseGet(() -> OwnerIncludingShop.of(owner, null));
            })
            .collect(Collectors.toList());

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
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(direction, "user.createdAt"));

        Page<Owner> result;

        if (ownersCondition.searchType() == OwnersCondition.SearchType.EMAIL) {
            result = adminOwnerRepository.findPageUnauthenticatedOwnersByEmail(ownersCondition.query(), pageRequest);
        } else if (ownersCondition.searchType() == OwnersCondition.SearchType.NAME) {
            result = adminOwnerRepository.findPageUnauthenticatedOwnersByName(ownersCondition.query(), pageRequest);
        } else {
            result = adminOwnerRepository.findPageUnauthenticatedOwners(pageRequest);
        }

        return result;
    }

    private Page<Owner> getOwnersResultPage(OwnersCondition ownersCondition, Criteria criteria,
        Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(direction, "user.createdAt"));

        Page<Owner> result;

        if (ownersCondition.searchType() == OwnersCondition.SearchType.EMAIL) {
            result = adminOwnerRepository.findPageOwnersByEmail(ownersCondition.query(), pageRequest);
        } else if (ownersCondition.searchType() == OwnersCondition.SearchType.NAME) {
            result = adminOwnerRepository.findPageOwnersByName(ownersCondition.query(), pageRequest);
        } else {
            result = adminOwnerRepository.findPageOwners(pageRequest);
        }

        return result;
    }
}
