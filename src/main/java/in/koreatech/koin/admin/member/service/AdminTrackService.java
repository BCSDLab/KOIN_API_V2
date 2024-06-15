package in.koreatech.koin.admin.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.member.dto.AdminTechStackRequest;
import in.koreatech.koin.admin.member.dto.AdminTechStackResponse;
import in.koreatech.koin.admin.member.dto.AdminTrackRequest;
import in.koreatech.koin.admin.member.dto.AdminTrackResponse;
import in.koreatech.koin.admin.member.dto.AdminTrackSingleResponse;
import in.koreatech.koin.admin.member.repository.AdminMemberRepository;
import in.koreatech.koin.admin.member.repository.AdminTechStackRepository;
import in.koreatech.koin.admin.member.repository.AdminTrackRepository;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.domain.member.model.Track;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTrackService {

    private final AdminTrackRepository adminTrackRepository;
    private final AdminMemberRepository adminMemberRepository;
    private final AdminTechStackRepository adminTechStackRepository;

    public List<AdminTrackResponse> getTracks() {
        return adminTrackRepository.findAll().stream()
            .map(AdminTrackResponse::from)
            .toList();
    }

    @Transactional
    public AdminTrackResponse createTrack(AdminTrackRequest request) {
        Track track = request.toEntity();
        Track savedTrack = adminTrackRepository.save(track);

        return AdminTrackResponse.from(savedTrack);
    }

    public AdminTrackSingleResponse getTrack(Integer trackId) {
        Track track = adminTrackRepository.getById(trackId);
        List<Member> member = adminMemberRepository.findByTrackId(trackId);
        List<TechStack> techStacks = adminTechStackRepository.findAllByTrackId(trackId);

        return AdminTrackSingleResponse.of(track, member, techStacks);
    }

    @Transactional
    public AdminTrackResponse updateTrack(Integer trackId, AdminTrackRequest request) {
        Track track = adminTrackRepository.getById(trackId);
        track.update(request.name(), request.headcount(), request.isDeleted());
        Track updatedTrack = adminTrackRepository.save(track);

        return AdminTrackResponse.from(updatedTrack);
    }

    @Transactional
    public AdminTechStackResponse createTechStack(AdminTechStackRequest request, String trackName) {
        Track track = adminTrackRepository.getByName(trackName);
        TechStack techStack = request.toEntity(track.getId());
        TechStack savedTechStack = adminTechStackRepository.save(techStack);

        return AdminTechStackResponse.from(savedTechStack);
    }

    @Transactional
    public AdminTechStackResponse updateTechStack(
        AdminTechStackRequest request,
        String trackName,
        Integer techStackId
    ) {
        TechStack techStack = adminTechStackRepository.getById(techStackId);

        Integer id = techStack.getTrackId();
        if (trackName != null) {
            Track track = adminTrackRepository.getByName(trackName);
            id = track.getId();
        }

        techStack.update(id, request.imageUrl(), request.name(), request.description(), request.isDeleted());
        TechStack updatedTechStack = adminTechStackRepository.save(techStack);
        return AdminTechStackResponse.from(updatedTechStack);
    }

    @Transactional
    public void deleteTechStack(Integer techStackId) {
        TechStack techStack = adminTechStackRepository.getById(techStackId);
        techStack.delete();
    }
}
