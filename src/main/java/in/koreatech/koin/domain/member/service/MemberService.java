package in.koreatech.koin.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.member.dto.MemberResponse;
import in.koreatech.koin.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public List<MemberResponse> getMembers() {
        var members = memberRepository.findAll();
        return members.stream()
            .map(MemberResponse::from)
            .toList();
    }

    public MemberResponse getMember(Integer id) {
        var member = memberRepository.getById(id);
        return MemberResponse.from(member);
    }
}
