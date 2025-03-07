package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import in.koreatech.koin.domain.ownershop.dto.CreateEventRequest;
import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopEventsResponse;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "(Normal) Owner Shop Event: 상점 이벤트 (점주 전용)", description = "사장님이 상점 이벤트 정보를 관리한다.")
public interface OwnerEventApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 이벤트 추가")
    @PostMapping("/owner/shops/{shopId}/event")
    ResponseEntity<Void> createShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @RequestBody @Valid CreateEventRequest shopEventRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 이벤트 수정")
    @PutMapping("/owner/shops/{shopId}/events/{eventId}")
    ResponseEntity<Void> modifyShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @PathVariable("eventId") Integer eventId,
        @RequestBody @Valid ModifyEventRequest modifyEventRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 이벤트 삭제")
    @DeleteMapping("/owner/shops/{shopId}/events/{eventId}")
    ResponseEntity<Void> deleteShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @PathVariable("eventId") Integer eventId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점 모든 이벤트 조회")
    @GetMapping("/owner/shops/{shopId}/event")
    ResponseEntity<OwnerShopEventsResponse> getShopAllEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId
    );
}
