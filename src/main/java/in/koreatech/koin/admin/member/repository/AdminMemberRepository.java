package in.koreatech.koin.admin.member.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.model.Member;

public interface AdminMemberRepository extends Repository<Member, Integer> {

    @EntityGraph(attributePaths = {"track"})
    @Query("select m from Member m where m.track.name = :trackName and m.isDeleted = :isDeleted")
    Page<Member> findAllByTrackAndIsDeleted(String trackName, Boolean isDeleted, Pageable pageable);

    @EntityGraph(attributePaths = {"track"})
    @Query("select count(m) from Member m where m.track.name = :trackName and m.isDeleted = :isDeleted")
    Integer countAllByTrackAndIsDeleted(String trackName, Boolean isDeleted);
}
