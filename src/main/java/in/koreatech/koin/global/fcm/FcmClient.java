package in.koreatech.koin.global.fcm;

import static com.google.firebase.messaging.AndroidConfig.Priority.HIGH;

import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.ApnsFcmOptions;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FcmClient {

    @Async
    public void sendMessage(
        String targetDeviceToken,
        String title,
        String content,
        String imageUrl,
        MobileAppPath path,
        String schemeUri,
        String type
    ) {
        if (targetDeviceToken == null) {
            return;
        }
        log.info("call FcmClient sendMessage: title: {}, content: {}", title, content);

        ApnsConfig apnsConfig = generateAppleConfig(title, content, imageUrl, path, type, schemeUri);
        AndroidConfig androidConfig = generateAndroidConfig(title, content, imageUrl, schemeUri, type);

        Message message = Message.builder()
            .setToken(targetDeviceToken)
            .setApnsConfig(apnsConfig)
            .setAndroidConfig(androidConfig).build();
        try {
            String result = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공: {}", result);
        } catch (Exception e) {
            log.warn("FCM 알림 전송 실패", e);
        }
    }

    private ApnsConfig generateAppleConfig(
        String title,
        String content,
        String imageUrl,
        MobileAppPath path,
        String type,
        String schemeUri
    ) {
        return ApnsConfig.builder()
            .setAps(
                Aps.builder()
                    .setAlert(
                        ApsAlert.builder()
                            .setTitle(title)
                            .setBody(content)
                            .build()
                    )
                    .setSound("default")
                    .setCategory(path != null ? path.getApple() : null)
                    .setMutableContent(true)
                    .build()
            )
            .setFcmOptions(
                ApnsFcmOptions.builder()
                    .setImage(imageUrl)
                    .build()
            )
            .putAllCustomData(
                Map.of(
                    "type", type,
                    "schemeUri", schemeUri
                )
            )
            .build();
    }

    private AndroidConfig generateAndroidConfig(
        String title,
        String content,
        String imageUrl,
        String schemeUri,
        String type
    ) {
        Map<String, String> androidNotificationV2 = new HashMap<>();
        androidNotificationV2.put("title", title != null ? title : "");
        androidNotificationV2.put("content", content != null ? content : "");
        androidNotificationV2.put("imageUrl", imageUrl != null ? imageUrl : "");
        androidNotificationV2.put("url", "koin://" + (schemeUri != null ? schemeUri : ""));
        androidNotificationV2.put("type", type != null ? type : "");

        return AndroidConfig.builder()
            .putAllData(androidNotificationV2)
            .setPriority(HIGH)
            .build();
    }
}
