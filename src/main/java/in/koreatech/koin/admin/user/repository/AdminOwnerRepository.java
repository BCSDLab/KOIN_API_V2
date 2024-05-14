package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.exception.OwnerNotFoundException;
import in.koreatech.koin.domain.owner.model.Owner;
import io.lettuce.core.dynamic.annotation.Param;

public interface AdminOwnerRepository extends Repository<Owner, Integer> {

    Optional<Owner> findById(Integer ownerId);

    Owner save(Owner owner);

    @Query("SELECT COUNT(o) FROM Owner o WHERE o.user.userType = 'OWNER' AND o.user.isAuthed = false")
    Integer findUnauthenticatedOwnersCount();

    @Query("SELECT o FROM Owner o WHERE o.user.userType = 'OWNER' AND o.user.isAuthed = false")
    Page<Owner> findPageUnauthenticatedOwners(Pageable pageable);

    @Query("SELECT o FROM Owner o WHERE o.user.userType = 'OWNER' AND o.user.isAuthed = false AND o.user.email LIKE CONCAT('%', :query, '%')")
    Page<Owner> findPageUnauthenticatedOwnersByEmail(@Param("query") String query, Pageable pageable);

    @Query("SELECT o FROM Owner o WHERE o.user.userType = 'OWNER' AND o.user.isAuthed = false AND o.user.name LIKE CONCAT('%', :query, '%')")
    Page<Owner> findPageUnauthenticatedOwnersByName(@Param("query") String query, Pageable pageable);

    default Owner getById(Integer ownerId) {
        return findById(ownerId).orElseThrow(() -> OwnerNotFoundException.withDetail("ownerId: " + ownerId));
    }
}
