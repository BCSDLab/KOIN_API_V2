package in.koreatech.koin.domain.member.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.model.Member;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    List<Member> findAllByTrackId(Long id);

    List<Member> findAll();
}
