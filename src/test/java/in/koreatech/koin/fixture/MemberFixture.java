package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.MemberRepository;
import in.koreatech.koin.domain.member.repository.TrackRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class MemberFixture {

    private final MemberRepository memberRepository;
    private final TrackRepository trackRepository;

    public MemberFixture(
        MemberRepository memberRepository,
        TrackRepository trackRepository
    ) {
        this.memberRepository = memberRepository;
        this.trackRepository = trackRepository;
    }

    public Member 최준호(Track track) {
        return memberRepository.save(
            Member.builder()
                .isDeleted(false)
                .studentNumber("2019136135")
                .imageUrl("https://imagetest.com/juno.jpg")
                .name("최준호")
                .position("Regular")
                .track(track)
                .email("testjuno@gmail.com")
                .build()
        );
    }

    public Member 박한수(Track track) {
        return memberRepository.save(
            Member.builder()
                .isDeleted(false)
                .studentNumber("2019136064")
                .imageUrl("https://imagetest.com/juno.jpg")
                .name("박한수")
                .position("Regular")
                .track(track)
                .email("testhsp@gmail.com")
                .build()
        );
    }
}
