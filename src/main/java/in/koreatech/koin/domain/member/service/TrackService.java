package in.koreatech.koin.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.member.dto.TrackResponse;
import in.koreatech.koin.domain.member.dto.TrackSingleResponse;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.MemberRepository;
import in.koreatech.koin.domain.member.repository.TechStackRepository;
import in.koreatech.koin.domain.member.repository.TrackRepository;
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

    public TrackSingleResponse getTrack(Integer id) {
        Track track = trackRepository.getById(id);
        List<Member> member = memberRepository.findAllByTrackIdAndIsDeletedFalse(id);
        List<TechStack> techStacks = techStackRepository.findAllByTrackId(id);

        return TrackSingleResponse.of(track, member, techStacks);
    }
}
