package in.koreatech.koin.admin.abtest.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.abtest.dto.AbtestAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestResponse;
import in.koreatech.koin.admin.abtest.service.AbtestService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import in.koreatech.koin.global.useragent.UserAgent;
import in.koreatech.koin.global.useragent.UserAgentInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/abtest")
public class AbtestController implements AbtestApi {

    private final AbtestService abtestService;

    // TODO: 지우기
    @GetMapping("/test")
    public ResponseEntity<Void> test(
        @UserAgent UserAgentInfo userAgentInfo,
        @IpAddress String ipAddress,
        @UserId Integer userId
    ) {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<AbtestResponse> createAbtest(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestBody @Valid AbtestRequest request
    ) {
        AbtestResponse response = abtestService.createAbtest(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMyAbtestVariable(
        @IpAddress String ipAddress,
        @RequestParam(name = "title") String title
    ) {
        String response = abtestService.getMyVariable(title, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignAbtestVariable(
        UserAgentInfo userAgentInfo,
        String ipAddress,
        Integer userId,
        AbtestAssignRequest abtestAssignRequest
    ) {
        String response = abtestService.assignVariable(userAgentInfo, ipAddress, userId, abtestAssignRequest);
        return ResponseEntity.ok(response);
    }
}
