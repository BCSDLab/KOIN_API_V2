package in.koreatech.koin.domain.track.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.track.domain.Member;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    List<Member> findAllByTrackId(Long id);
}
