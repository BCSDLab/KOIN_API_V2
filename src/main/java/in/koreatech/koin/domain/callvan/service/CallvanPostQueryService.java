package in.koreatech.koin.domain.callvan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.callvan.dto.CallvanPostSearchResponse;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.filter.CallvanAuthorFilter;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostSortCriteria;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostStatusFilter;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostQueryRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanPostQueryService {

    private final CallvanPostQueryRepository callvanPostQueryRepository;
    private final CallvanParticipantRepository callvanParticipantRepository;

    public CallvanPostSearchResponse getCallvanPosts(
        CallvanAuthorFilter authorFilter,
        List<CallvanLocation> departures,
        String departureKeyword,
        List<CallvanLocation> arrivals,
        String arrivalKeyword,
        CallvanPostStatusFilter statusFilter,
        String title,
        CallvanPostSortCriteria sort,
        Integer page,
        Integer limit,
        Integer userId
    ) {
        Integer authorId = authorFilter.getRequiredAuthorId(userId);

        Long totalCount = callvanPostQueryRepository.countCallvanPosts(
            authorId, departures, departureKeyword, arrivals, arrivalKeyword, statusFilter.getStatus(), title);

        Criteria criteria = Criteria.of(page, limit, totalCount.intValue());

        List<CallvanPost> posts = callvanPostQueryRepository.findCallvanPosts(
            authorId, departures, departureKeyword, arrivals, arrivalKeyword, statusFilter.getStatus(), title, sort,
            criteria);

        int totalPage = (int)Math.ceil((double)totalCount / criteria.getLimit());
        if (totalPage == 0)
            totalPage = 1;

        Set<Integer> joinedPostIds = Collections.emptySet();
        if (userId != null && !posts.isEmpty()) {
            List<Integer> postIds = posts.stream()
                .map(CallvanPost::getId)
                .toList();
            List<CallvanParticipant> participants = callvanParticipantRepository.findAllByMemberIdAndPostIdIn(userId,
                postIds);
            joinedPostIds = participants.stream()
                .map(participant -> participant.getPost().getId())
                .collect(Collectors.toSet());
        }

        return CallvanPostSearchResponse.of(
            posts,
            totalCount,
            criteria.getPage() + 1,
            totalPage,
            joinedPostIds,
            userId
        );
    }
}
