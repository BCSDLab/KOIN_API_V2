package in.koreatech.koin.infrastructure.fcm;

import static com.google.firebase.messaging.AndroidConfig.Priority.HIGH;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.ApnsFcmOptions;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import in.koreatech.koin.common.model.MobileAppPath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FcmClient {

    private static final int FCM_MESSAGE_BATCH_SIZE = 500;

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
        sendMessageWithResult(targetDeviceToken, title, content, imageUrl, path, schemeUri, type);
    }

    public boolean sendMessageWithResult(
        String targetDeviceToken,
        String title,
        String content,
        String imageUrl,
        MobileAppPath path,
        String schemeUri,
        String type
    ) {
        if (!StringUtils.hasText(targetDeviceToken)) {
            return false;
        }
        try {
            log.info("call FcmClient sendMessage: title: {}, content: {}", title, content);

            ApnsConfig apnsConfig = generateAppleConfig(title, content, imageUrl, path, type, schemeUri);
            AndroidConfig androidConfig = generateAndroidConfig(title, content, imageUrl, schemeUri, type);

            Message message = Message.builder()
                .setToken(targetDeviceToken)
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(androidConfig)
                .build();

            String result = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공: {}", result);
            return true;
        } catch (Exception e) {
            log.warn("FCM 알림 전송 실패", e);
            return false;
        }
    }

    public void sendMessages(List<FcmSendRequest> requests) {
        try {
            if (requests.size() > FCM_MESSAGE_BATCH_SIZE) {
                log.warn("FCM 전송 최대 개수를 초과했습니다.");
                return ;
            }

            List<Message> messages = requests.stream()
                .map(request -> Message.builder()
                    .setToken(request.targetDeviceToken())
                    .setApnsConfig(generateAppleConfig(
                        request.title(),
                        request.content(),
                        request.imageUrl(),
                        request.path(),
                        request.type(),
                        request.schemeUri()
                    ))
                    .setAndroidConfig(generateAndroidConfig(
                        request.title(),
                        request.content(),
                        request.imageUrl(),
                        request.schemeUri(),
                        request.type()
                    ))
                    .build()
                )
                .toList();

            FirebaseMessaging.getInstance().sendEach(messages);
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
                    .setCategory(path != null ? path.getPath() : null)
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
