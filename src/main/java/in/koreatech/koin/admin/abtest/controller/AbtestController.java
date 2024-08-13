package in.koreatech.koin.admin.abtest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.abtest.service.AbtestService;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import in.koreatech.koin.global.useragent.UserAgent;
import in.koreatech.koin.global.useragent.UserAgentInfo;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/abtest")
public class AbtestController implements AbtestApi {

    private final AbtestService abtestService;

    @GetMapping("/test")
    public ResponseEntity<Void> test(
        @UserAgent UserAgentInfo userAgentInfo,
        @IpAddress String ipAddress,
        @UserId Integer userId
    ) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMyAbtestVariable(
        @IpAddress String ipAddress,
        @RequestParam(name = "title") String title
    ) {
        var response = abtestService.getMyVariable(title, ipAddress);
        return ResponseEntity.ok(response);
    }
}
