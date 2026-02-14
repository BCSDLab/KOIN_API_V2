package in.koreatech.koin.domain.callvan.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

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
import in.koreatech.koin.domain.callvan.dto.CallvanPostDetailResponse;
import in.koreatech.koin.domain.callvan.dto.CallvanPostSearchResponse;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.filter.CallvanAuthorFilter;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostSortCriteria;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostStatusFilter;
import in.koreatech.koin.domain.callvan.service.CallvanPostQueryService;
import in.koreatech.koin.domain.callvan.service.CallvanPostCreateService;
import in.koreatech.koin.domain.callvan.service.CallvanPostJoinService;
import in.koreatech.koin.domain.callvan.service.CallvanPostStatusService;
import in.koreatech.koin.domain.callvan.service.CallvanChatService;
import in.koreatech.koin.domain.callvan.service.CallvanNotificationService;
import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanNotificationResponse;
import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/callvan")
@RequiredArgsConstructor
public class CallvanController implements CallvanApi {

    private final CallvanPostCreateService callvanPostCreateService;
    private final CallvanPostQueryService callvanPostQueryService;
    private final CallvanPostJoinService callvanPostJoinService;
    private final CallvanPostStatusService callvanPostStatusService;
    private final CallvanChatService callvanChatService;
    private final CallvanNotificationService callvanNotificationService;

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
        List<CallvanPostStatusFilter> statuses,
        String title,
        CallvanPostSortCriteria sort,
        Integer page,
        Integer limit,
        @UserId Integer userId
    ) {
        CallvanPostSearchResponse response = callvanPostQueryService.getCallvanPosts(
            author, departures, departureKeyword, arrivals, arrivalKeyword, statuses, title, sort, page, limit,
            userId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<CallvanPostDetailResponse> getCallvanPostDetail(
        @PathVariable Integer postId,
        @UserId Integer userId
    ) {
        CallvanPostDetailResponse response = callvanPostQueryService.getCallvanPostDetail(postId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/{postId}/participants")
    public ResponseEntity<Void> joinCallvanPost(
        @PathVariable Integer postId,
        @UserId Integer userId
    ) {
        callvanPostJoinService.join(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/posts/{postId}/participants")
    public ResponseEntity<Void> leaveCallvanPost(
        @PathVariable Integer postId,
        @UserId Integer userId
    ) {
        callvanPostJoinService.leave(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/posts/{postId}/chat")
    public ResponseEntity<CallvanChatMessageResponse> getCallvanChatMessages(
        @PathVariable Integer postId,
        @UserId Integer userId
    ) {
        CallvanChatMessageResponse response = callvanChatService.getMessages(postId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/{postId}/chat")
    public ResponseEntity<Void> sendMessage(
        @PathVariable Integer postId,
        @RequestBody CallvanChatMessageRequest request,
        @UserId Integer userId
    ) {
        callvanChatService.sendMessage(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/posts/{postId}/close")
    public ResponseEntity<Void> closeCallvanPost(
        @PathVariable Integer postId,
        @UserId Integer userId
    ) {
        callvanPostStatusService.close(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/posts/{postId}/reopen")
    public ResponseEntity<Void> reopenCallvanPost(
        @PathVariable Integer postId,
        @UserId Integer userId
    ) {
        callvanPostStatusService.reopen(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/posts/{postId}/complete")
    public ResponseEntity<Void> completeCallvanPost(
        @PathVariable Integer postId,
        @UserId Integer userId
    ) {
        callvanPostStatusService.complete(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<CallvanNotificationResponse>> getNotifications(
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        List<CallvanNotificationResponse> responses = callvanNotificationService.getNotifications(userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/notifications/mark-all-read")
    public ResponseEntity<Void> markAllNotificationsAsRead(
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        callvanNotificationService.markAllRead(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
