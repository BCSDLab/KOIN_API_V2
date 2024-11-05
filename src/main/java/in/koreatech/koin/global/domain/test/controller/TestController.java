package in.koreatech.koin.global.domain.test.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.global.domain.notification.model.NotificationType;
import in.koreatech.koin.global.fcm.FcmClient;
import in.koreatech.koin.global.fcm.MobileAppPath;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController implements TestApi {

    private final FcmClient fcmClient;

    @GetMapping("/notification")
    public ResponseEntity<Void> testSendMessage(
        @RequestParam String deviceToken,
        @RequestParam String title,
        @RequestParam String body,
        @RequestParam String image,
        @RequestParam(defaultValue = "HOME") MobileAppPath appPath,
        @RequestParam String uri
    ) {
        fcmClient.sendMessage(
            deviceToken,
            title,
            body,
            image,
            appPath,
            uri,
            NotificationType.MESSAGE.name().toLowerCase()
        );
        return ResponseEntity.ok().build();
    }
}
