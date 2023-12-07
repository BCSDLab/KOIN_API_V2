package in.koreatech.koin.service;

import in.koreatech.koin.domain.Member;
import in.koreatech.koin.domain.TechStack;
import in.koreatech.koin.domain.Track;
import in.koreatech.koin.dto.TrackResponse;
import in.koreatech.koin.dto.TrackSingleResponse;
import in.koreatech.koin.repository.MemberRepository;
import in.koreatech.koin.repository.TechStackRepository;
import in.koreatech.koin.repository.TrackRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final MemberRepository memberRepository;
    private final TechStackRepository techStackRepository;

    public List<TrackResponse> getTracks() {
        return trackRepository.findAll().stream()
            .map(TrackResponse::from)
            .toList();
    }

    public TrackSingleResponse getTrack(Long id) {
        Track track = trackRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랙입니다."));
        List<Member> member = memberRepository.findAllByTrackId(id);
        List<TechStack> techStacks = techStackRepository.findAllByTrackId(id);

        return TrackSingleResponse.of(track, member, techStacks);
    }
}
