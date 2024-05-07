package in.koreatech.koin.admin.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.member.dto.AdminMembersResponse;
import in.koreatech.koin.admin.member.enums.TrackTag;
import in.koreatech.koin.admin.member.repository.AdminMemberRepository;
import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final AdminMemberRepository adminMemberRepository;

    public AdminMembersResponse getMembers(Integer page, Integer limit, TrackTag track, Boolean isDeleted) {
        Integer total = adminMemberRepository.countAllByTrackAndIsDeleted(track.getTag(), isDeleted);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), Sort.by(Sort.Direction.ASC, "id"));

        Page<Member> result = adminMemberRepository.findAllByTrackAndIsDeleted(track.getTag(), isDeleted, pageRequest);

        return AdminMembersResponse.of(result, criteria);
    }

}