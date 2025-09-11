package in.koreatech.koin.domain.club.event.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.club.aop.ClubManagerOnly;
import in.koreatech.koin.domain.club.event.dto.request.ClubEventCreateRequest;
import in.koreatech.koin.domain.club.event.dto.request.ClubEventModifyRequest;
import in.koreatech.koin.domain.club.event.dto.response.ClubEventResponse;
import in.koreatech.koin.domain.club.event.dto.response.ClubEventsResponse;
import in.koreatech.koin.domain.club.event.enums.ClubEventType;
import in.koreatech.koin.domain.club.event.service.ClubEventService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs/{clubId}")
public class ClubEventController implements ClubEventApi {

    private final ClubEventService clubEventService;

    @ClubManagerOnly
    @PostMapping
    public ResponseEntity<Void> createClubEvent(
        @PathVariable Integer clubId,
        @RequestBody @Valid ClubEventCreateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubEventService.createClubEvent(request, clubId, studentId);
        return ResponseEntity.ok().build();
    };

    @ClubManagerOnly
    @PutMapping("/event/{eventId}")
    public ResponseEntity<Void> modifyClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @RequestBody @Valid ClubEventModifyRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubEventService.modifyClubEvent(request, eventId, clubId, studentId);
        return ResponseEntity.ok().build();
    }

    @ClubManagerOnly
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Void> deleteClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubEventService.deleteClubEvent(clubId, eventId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ClubEventResponse> getClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId
    ) {
        ClubEventResponse response = clubEventService.getClubEvent(clubId, eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events")
    public ResponseEntity<List<ClubEventsResponse>> getClubEvents(
        @PathVariable Integer clubId,
        @RequestParam(defaultValue = "RECENT") ClubEventType eventType,
        @UserId Integer userId
    ) {
        List<ClubEventsResponse> responses = clubEventService.getClubEvents(clubId, eventType, userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/event/{eventId}/notification")
    public ResponseEntity<Void> subscribeEventNotification(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubEventService.subscribeEventNotification(clubId, eventId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/event/{eventId}/notification")
    public ResponseEntity<Void> rejectEventNotification(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubEventService.rejectEventNotification(clubId, eventId, studentId);
        return ResponseEntity.noContent().build();
    }
}
