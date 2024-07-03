package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.exception.OwnerNotFoundException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.user.model.UserType;
import io.lettuce.core.dynamic.annotation.Param;

public interface AdminOwnerRepository extends Repository<Owner, Integer> {

    Optional<Owner> findById(Integer ownerId);

    Owner save(Owner owner);

    void deleteById(Integer ownerId);

    @Query("""
        SELECT COUNT(o) FROM Owner o 
        WHERE o.user.userType = 'OWNER' 
        AND o.user.isAuthed = false
        """)
    Integer findUnauthenticatedOwnersCount();

    Integer countByUserUserType(UserType userType);

    Page<Owner> findPageOwners(Pageable pageable);

    @Query("""
        SELECT Owner FROM Owner o
        WHERE o.user.email LIKE CONCAT('%', :query, '%')
    """)
    Page<Owner> findPageOwnersByEmail(@Param("query") String query, Pageable pageable);

    @Query("""
        SELECT Owner FROM Owner o
        WHERE o.user.name LIKE CONCAT('%', :query, '%')
    """)
    Page<Owner> findPageOwnersByName(@Param("query") String query, Pageable pageable);

    default Owner getById(Integer ownerId) {
        return findById(ownerId).orElseThrow(() -> OwnerNotFoundException.withDetail("ownerId: " + ownerId));
    }
}
