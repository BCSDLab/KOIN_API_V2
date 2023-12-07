package in.koreatech.koin.repository;

import in.koreatech.koin.domain.Member;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    List<Member> findAllByTrackId(Long id);
}
