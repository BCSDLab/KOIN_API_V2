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
import in.koreatech.koin.domain.callvan.service.CallvanQueryService;
import in.koreatech.koin.domain.callvan.service.CallvanService;
import in.koreatech.koin.global.auth.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/callvan")
@RequiredArgsConstructor
public class CallvanController implements CallvanApi {

    private final CallvanService callvanService;
    private final CallvanQueryService callvanQueryService;

    @PostMapping
    public ResponseEntity<CallvanPostCreateResponse> createCallvanPost(
        @RequestBody CallvanPostCreateRequest request,
        Integer userId
    ) {
        CallvanPostCreateResponse response = callvanService.createCallvanPost(request, userId);
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
        CallvanPostSearchResponse response = callvanQueryService.getCallvanPosts(
            author, departures, departureKeyword, arrivals, arrivalKeyword, status, title, sort, page, limit, userId
        );
        return ResponseEntity.ok().body(response);
    }
}
