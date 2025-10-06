package in.koreatech.koin.admin.owner.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.exception.OwnerNotFoundException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

import org.springframework.data.repository.query.Param;

@JpaRepositoryMarker
public interface AdminOwnerRepository extends Repository<Owner, Integer> {

    Optional<Owner> findById(Integer ownerId);

    Owner save(Owner owner);

    void deleteById(Integer ownerId);

    Integer countByUserUserType(UserType userType);

    @Query("""
        SELECT o FROM Owner o
        JOIN o.user u
        WHERE u.isAuthed = true
        """)
    Page<Owner> findPageOwners(Pageable pageable);

    @Query("""
        SELECT o FROM Owner o
        JOIN o.user u
        WHERE u.isAuthed = true
        AND u.email LIKE CONCAT('%', :query, '%')
        """)
    Page<Owner> findPageOwnersByEmail(@Param("query") String query, Pageable pageable);

    @Query("""
        SELECT o FROM Owner o
        JOIN o.user u
        WHERE u.isAuthed = true
        AND u.name LIKE CONCAT('%', :query, '%')
        """)
    Page<Owner> findPageOwnersByName(@Param("query") String query, Pageable pageable);

    @Query("""
        SELECT o FROM Owner o
        JOIN o.user u
        WHERE u.isAuthed = false
        """)
    Page<Owner> findPageUnauthenticatedOwners(Pageable pageable);

    @Query("""
        SELECT o FROM Owner o
        JOIN o.user u
        WHERE u.isAuthed = false
        AND u.email LIKE CONCAT('%', :query, '%')
        """)
    Page<Owner> findPageUnauthenticatedOwnersByEmail(@Param("query") String query, Pageable pageable);

    @Query("""
        SELECT o FROM Owner o
        JOIN o.user u
        WHERE u.isAuthed = false
        AND u.name LIKE CONCAT('%', :query, '%')
        """)
    Page<Owner> findPageUnauthenticatedOwnersByName(@Param("query") String query, Pageable pageable);

    default Owner getById(Integer ownerId) {
        return findById(ownerId).orElseThrow(() -> OwnerNotFoundException.withDetail("ownerId: " + ownerId));
    }
}
