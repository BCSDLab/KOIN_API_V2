package in.koreatech.koin.admin.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.member.dto.AdminMemberRequest;
import in.koreatech.koin.admin.member.dto.AdminMemberResponse;
import in.koreatech.koin.admin.member.dto.AdminMembersResponse;
import in.koreatech.koin.admin.member.enums.TrackTag;
import in.koreatech.koin.admin.member.repository.AdminMemberRepository;
import in.koreatech.koin.admin.member.repository.AdminTrackRepository;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin._common.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final AdminMemberRepository adminMemberRepository;
    private final AdminTrackRepository adminTrackRepository;

    public AdminMembersResponse getMembers(Integer page, Integer limit, TrackTag track, Boolean isDeleted) {
        Integer total = adminMemberRepository.countAllByTrackAndIsDeleted(track.getTag(), isDeleted);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<Member> result = adminMemberRepository.findAllByTrackAndIsDeleted(track.getTag(), isDeleted, pageRequest);

        return AdminMembersResponse.of(result, criteria);
    }

    @Transactional
    public void createMember(AdminMemberRequest request) {
        Track track = adminTrackRepository.getByName(request.track());
        Member member = request.toMember(track);
        adminMemberRepository.save(member);
    }

    public AdminMemberResponse getMember(Integer memberId) {
        Member member = adminMemberRepository.getById(memberId);
        return AdminMemberResponse.from(member);
    }

    @Transactional
    public void deleteMember(Integer memberId) {
        Member member = adminMemberRepository.getById(memberId);
        member.delete();
    }

    @Transactional
    public void updateMember(Integer memberId, AdminMemberRequest request) {
        Member member = adminMemberRepository.getById(memberId);

        String currentTrackName = member.getTrack().getName();
        String changedTrackName = request.track();
        if (!currentTrackName.equals(changedTrackName)) {
            member.updateTrack(adminTrackRepository.getByName(request.track()));
        }

        member.update(request.name(), request.studentNumber(), request.position(), request.email(), request.imageUrl());
    }

    @Transactional
    public void undeleteMember(Integer memberId) {
        Member member = adminMemberRepository.getById(memberId);
        member.undelete();
    }
}
