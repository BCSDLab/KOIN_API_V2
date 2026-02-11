package in.koreatech.koin.domain.callvan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateResponse;
import in.koreatech.koin.domain.callvan.dto.CallvanPostSearchResponse;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.filter.CallvanAuthorFilter;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostSortCriteria;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostStatusFilter;
import in.koreatech.koin.domain.callvan.service.CallvanPostQueryService;
import in.koreatech.koin.domain.callvan.service.CallvanPostCreateService;
import in.koreatech.koin.domain.callvan.service.CallvanPostJoinService;
import in.koreatech.koin.global.auth.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/callvan")
@RequiredArgsConstructor
public class CallvanController implements CallvanApi {

    private final CallvanPostCreateService callvanPostCreateService;
    private final CallvanPostQueryService callvanPostQueryService;
    private final CallvanPostJoinService callvanPostJoinService;

    @PostMapping
    public ResponseEntity<CallvanPostCreateResponse> createCallvanPost(
        @RequestBody CallvanPostCreateRequest request,
        Integer userId
    ) {
        CallvanPostCreateResponse response = callvanPostCreateService.createCallvanPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CallvanPostSearchResponse> getCallvanPosts(
        CallvanAuthorFilter author,
        List<CallvanLocation> departures,
        String departureKeyword,
        List<CallvanLocation> arrivals,
        String arrivalKeyword,
        CallvanPostStatusFilter status,
        String title,
        CallvanPostSortCriteria sort,
        Integer page,
        Integer limit,
        @UserId Integer userId
    ) {
        CallvanPostSearchResponse response = callvanPostQueryService.getCallvanPosts(
            author, departures, departureKeyword, arrivals, arrivalKeyword, status, title, sort, page, limit, userId
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/posts/{postId}/participants")
    public ResponseEntity<Void> joinCallvanPost(
            @PathVariable Integer postId,
            @UserId Integer userId
    ) {
        callvanPostJoinService.join(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
