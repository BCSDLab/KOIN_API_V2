package in.koreatech.koin.domain.test;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.notification.model.NotificationType;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import lombok.RequiredArgsConstructor;

@Profile("dev")
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

    @GetMapping("/service-unavailable")
    public ResponseEntity<Object> testServiceUnavailable() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "서비스 점검 중입니다.");
        response.put("start_time", "2025-04-10T00:00");
        response.put("end_time", "2026-12-31T23:59");

        return ResponseEntity.status(SERVICE_UNAVAILABLE).body(response);
    }
}
