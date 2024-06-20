package in.koreatech.koin.domain.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.exception.MemberNotFoundException;
import in.koreatech.koin.domain.member.model.Member;

public interface MemberRepository extends Repository<Member, Integer> {

    Member save(Member member);

    List<Member> findAllByTrackIdAndIsDeletedFalse(Integer id);

    List<Member> findAll();

    Optional<Member> findById(Integer id);

    default Member getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> MemberNotFoundException.withDetail("id: " + id));
    }
}
