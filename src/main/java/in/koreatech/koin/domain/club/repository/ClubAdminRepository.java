package in.koreatech.koin.domain.club.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubAdmin;

public interface ClubAdminRepository extends Repository<ClubAdmin, Integer> {

    ClubAdmin save(ClubAdmin clubAdmin);

    @Query("""
        SELECT ca FROM ClubAdmin ca
        JOIN FETCH ca.user u
        JOIN FETCH ca.club c
        """)
    Page<ClubAdmin> findPageAll(Pageable pageable);

    @Query("""
        SELECT COUNT(ca) 
        FROM ClubAdmin ca 
        JOIN ca.user u
        """)
    int countAll();

    boolean existsByClubIdAndUserId(Integer clubId, Integer studentId);
}
