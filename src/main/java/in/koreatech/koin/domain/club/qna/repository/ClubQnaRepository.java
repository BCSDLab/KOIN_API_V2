package in.koreatech.koin.domain.club.qna.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.qna.exception.ClubQnaNotFoundException;
import in.koreatech.koin.domain.club.qna.model.ClubQna;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface ClubQnaRepository extends Repository<ClubQna, Integer> {

    List<ClubQna> findAllByClubId(Integer clubId);

    Optional<ClubQna> findById(Integer id);

    default ClubQna getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> ClubQnaNotFoundException.withDetail("id : " + id));
    }

    ClubQna save(ClubQna qna);

    void delete(ClubQna qna);
}
