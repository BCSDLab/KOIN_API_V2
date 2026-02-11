package in.koreatech.koin.domain.callvan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.callvan.dto.CallvanPostSearchResponse;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.filter.CallvanAuthorFilter;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostSortCriteria;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostStatusFilter;
import in.koreatech.koin.domain.callvan.repository.CallvanPostQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanQueryService {

    private final CallvanPostQueryRepository callvanPostQueryRepository;

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

        return CallvanPostSearchResponse.of(
            posts,
            totalCount,
            criteria.getPage() + 1,
            totalPage
        );
    }
}
