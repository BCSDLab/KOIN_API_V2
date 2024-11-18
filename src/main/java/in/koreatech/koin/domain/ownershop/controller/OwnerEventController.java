package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import in.koreatech.koin.domain.ownershop.dto.CreateEventRequest;
import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopEventsResponse;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.service.OwnerShopService;
import in.koreatech.koin.domain.shop.dto.menu.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.shop.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.shop.ShopResponse;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class OwnerEventController implements OwnerEventApi {

    private final OwnerShopService ownerShopService;

    @PostMapping("/owner/shops/{id}/event")
    public ResponseEntity<Void> createShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateEventRequest shopEventRequest
    ) {
        ownerShopService.createEvent(ownerId, shopId, shopEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/{shopId}/events/{eventId}")
    public ResponseEntity<Void> modifyShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @PathVariable("eventId") Integer eventId,
        @RequestBody @Valid ModifyEventRequest modifyEventRequest
    ) {
        ownerShopService.modifyEvent(ownerId, shopId, eventId, modifyEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/owner/shops/{shopId}/events/{eventId}")
    public ResponseEntity<Void> deleteShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @PathVariable("eventId") Integer eventId
    ) {
        ownerShopService.deleteEvent(ownerId, shopId, eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/owner/shops/{shopId}/event")
    public ResponseEntity<OwnerShopEventsResponse> getShopAllEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId
    ) {
        OwnerShopEventsResponse shopEventsResponse = ownerShopService.getShopEvent(shopId, ownerId);
        return ResponseEntity.ok(shopEventsResponse);
    }
}
