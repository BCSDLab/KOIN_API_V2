package in.koreatech.koin.global.fcm;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

import static com.google.firebase.messaging.AndroidConfig.Priority.HIGH;

import java.util.List;

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
        String url,
        String type
    ) {
        if (targetDeviceToken == null) {
            return;
        }
        log.info("call FcmClient sendMessage: title: {}, content: {}", title, content);
        Notification notification = Notification.builder()
            .setTitle(title)
            .setBody(content)
            .setImage(imageUrl)
            .build();
        Message message = Message.builder()
            .setToken(targetDeviceToken)
            .setNotification(notification)
            .putData("url", url)
            .putData("type", type)
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(HIGH)
                .build()
            ).build();
        try {
            String result = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공: {}", result);
        } catch (Exception e) {
            log.warn("FCM 알림 전송 실패", e);
        }
    }

    @Async
    public void sendMessageAll(
        List<String> targetDeviceTokens,
        String title,
        String content,
        String imageUrl,
        String url,
        String type
    ) {
        if (targetDeviceTokens.isEmpty()) {
            return;
        }
        log.info("call FcmClient sendMessage: title: {}, content: {}", title, content);
        Notification notification = Notification.builder()
            .setTitle(title)
            .setBody(content)
            .setImage(imageUrl)
            .build();
        MulticastMessage message = MulticastMessage.builder()
            .addAllTokens(targetDeviceTokens)
            .putData("url", url)
            .putData("type", type)
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(HIGH)
                .build()
            ).build();
        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM 알림 전송 성공: {}", response.getSuccessCount());
        } catch (Exception e) {
            log.warn("FCM 알림 전송 실패", e);
        }
    }
}
