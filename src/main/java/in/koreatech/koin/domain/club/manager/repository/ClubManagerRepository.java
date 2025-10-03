package in.koreatech.koin.domain.club.manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.manager.model.ClubManager;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface ClubManagerRepository extends Repository<ClubManager, Integer> {

    ClubManager save(ClubManager clubManager);

    @Query("""
        SELECT ca FROM ClubManager ca
        JOIN FETCH ca.user u
        JOIN FETCH ca.club c
        """)
    Page<ClubManager> findPageAll(Pageable pageable);

    @Query("""
        SELECT COUNT(ca) 
        FROM ClubManager ca 
        JOIN ca.user u
        """)
    int countAll();

    boolean existsByClubIdAndUserId(Integer clubId, Integer studentId);

    boolean existsByClubAndUser(Club club, User user);

    void deleteByClubAndUser(Club club, User user);
}
