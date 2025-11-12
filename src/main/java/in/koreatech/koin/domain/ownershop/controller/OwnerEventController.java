package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import in.koreatech.koin.domain.ownershop.dto.CreateEventRequest;
import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopEventsResponse;
import in.koreatech.koin.domain.ownershop.service.OwnerEventService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OwnerEventController implements OwnerEventApi {

    private final OwnerEventService ownerEventService;

    @PostMapping("/owner/shops/{id}/event")
    public ResponseEntity<Void> createShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateEventRequest shopEventRequest
    ) {
        ownerEventService.createEvent(ownerId, shopId, shopEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/{shopId}/events/{eventId}")
    public ResponseEntity<Void> modifyShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @PathVariable("eventId") Integer eventId,
        @RequestBody @Valid ModifyEventRequest modifyEventRequest
    ) {
        ownerEventService.modifyEvent(ownerId, shopId, eventId, modifyEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/owner/shops/{shopId}/events/{eventId}")
    public ResponseEntity<Void> deleteShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @PathVariable("eventId") Integer eventId
    ) {
        ownerEventService.deleteEvent(ownerId, shopId, eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/owner/shops/{shopId}/event")
    public ResponseEntity<OwnerShopEventsResponse> getShopAllEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId
    ) {
        OwnerShopEventsResponse shopEventsResponse = ownerEventService.getShopEvent(shopId, ownerId);
        return ResponseEntity.ok(shopEventsResponse);
    }
}
