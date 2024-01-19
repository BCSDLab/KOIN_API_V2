package in.koreatech.koin.domain.track.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.track.dto.TrackResponse;
import in.koreatech.koin.domain.track.dto.TrackSingleResponse;
import in.koreatech.koin.domain.track.model.Member;
import in.koreatech.koin.domain.track.model.TechStack;
import in.koreatech.koin.domain.track.model.Track;
import in.koreatech.koin.domain.track.repository.MemberRepository;
import in.koreatech.koin.domain.track.repository.TechStackRepository;
import in.koreatech.koin.domain.track.repository.TrackRepository;
import lombok.RequiredArgsConstructor;

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
        Track track = trackRepository.getById(id);
        List<Member> member = memberRepository.findAllByTrackId(id);
        List<TechStack> techStacks = techStackRepository.findAllByTrackId(id);

        return TrackSingleResponse.of(track, member, techStacks);
    }
}
