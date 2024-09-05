package in.koreatech.koin.admin.abtest.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.abtest.dto.AbtestAdminAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestCloseRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestDevicesResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestUsersResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestsResponse;
import in.koreatech.koin.admin.abtest.service.AbtestService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.useragent.UserAgent;
import in.koreatech.koin.global.useragent.UserAgentInfo;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/abtest")
public class AbtestController implements AbtestApi {

    private final AbtestService abtestService;

    @PostMapping
    public ResponseEntity<AbtestResponse> createAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestBody @Valid AbtestRequest request
    ) {
        AbtestResponse response = abtestService.createAbtest(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AbtestResponse> putAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable("id") Integer abtestId,
        @RequestBody @Valid AbtestRequest request
    ) {
        AbtestResponse response = abtestService.putAbtest(abtestId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable("id") Integer abtestId
    ) {
        abtestService.deleteAbtest(abtestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<AbtestsResponse> getAbtests(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        AbtestsResponse response = abtestService.getAbtests(page, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbtestResponse> getAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @Parameter(in = PATH) @PathVariable("id") Integer abtestId
    ) {
        AbtestResponse response = abtestService.getAbtest(abtestId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/close/{id}")
    public ResponseEntity<Void> closeAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable("id") Integer abtestId,
        @RequestBody @Valid AbtestCloseRequest abtestCloseRequest
    ) {
        abtestService.closeAbtest(abtestId, abtestCloseRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<AbtestUsersResponse> getUsersByName(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(value = "name") String userName
    ) {
        AbtestUsersResponse response = abtestService.getUsersByName(userName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/device")
    public ResponseEntity<AbtestDevicesResponse> getDevicesByUserId(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable(value = "userId") Integer userId
    ) {
        AbtestDevicesResponse response = abtestService.getDevicesByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<Void> assignAbtestVariableByAdmin(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable(value = "id") Integer abtestId,
        @RequestBody @Valid AbtestAdminAssignRequest abtestAdminAssignRequest
    ) {
        abtestService.assignVariableByAdmin(abtestId, abtestAdminAssignRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMyAbtestVariable(
        @RequestHeader("accessHistoryId") Integer accessHistoryId,
        @UserId Integer userId,
        @RequestParam(name = "title") String title
    ) {
        String response = abtestService.getMyVariable(accessHistoryId, userId, title);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignAbtestVariable(
        @RequestHeader("accessHistoryId") Integer accessHistoryId,
        @UserAgent UserAgentInfo userAgentInfo,
        @UserId Integer userId,
        @RequestBody @Valid AbtestAssignRequest abtestAssignRequest
    ) {
        String response = abtestService.assignVariable(accessHistoryId, userAgentInfo, userId, abtestAssignRequest);
        return ResponseEntity.ok(response);
    }
}
