package in.koreatech.koin.admin.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.exception.MemberNotFoundException;
import in.koreatech.koin.domain.member.model.Member;
import org.springframework.data.repository.query.Param;

public interface AdminMemberRepository extends Repository<Member, Integer> {

    @EntityGraph(attributePaths = {"track"})
    @Query("select m from Member m where m.track.name = :trackName and m.isDeleted = :isDeleted")
    Page<Member> findAllByTrackAndIsDeleted(
            @Param("trackName") String trackName,
            @Param("isDeleted") Boolean isDeleted,
            Pageable pageable);

    @EntityGraph(attributePaths = {"track"})
    @Query("select count(m) from Member m where m.track.name = :trackName and m.isDeleted = :isDeleted")
    Integer countAllByTrackAndIsDeleted(
            @Param("trackName") String trackName,
            @Param("isDeleted") Boolean isDeleted);

    Member save(Member member);

    List<Member> findByTrackId(Integer id);

    @EntityGraph(attributePaths = {"track"})
    Optional<Member> findByName(String name);

    default Member getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> MemberNotFoundException.withDetail("name: " + name));
    }

    @EntityGraph(attributePaths = {"track"})
    Optional<Member> findById(Integer id);

    default Member getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> MemberNotFoundException.withDetail("id: " + id));
    }
}
