package in.koreatech.koin.infrastructure.fcm;

import static com.google.firebase.messaging.AndroidConfig.Priority.HIGH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.ApnsFcmOptions;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.SendResponse;

import in.koreatech.koin.common.model.MobileAppPath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FcmClient {

    private static final int FCM_MESSAGE_BATCH_SIZE = 500;

    @Async
    public void sendMessage(FcmSendRequest request) {
        if (!StringUtils.hasText(request.targetDeviceToken())) {
            return;
        }
        try {
            log.info("call FcmClient sendMessage: title: {}, content: {}", request.title(), request.content());
            String result = FirebaseMessaging.getInstance().send(buildMessage(request));
            log.info("FCM 알림 전송 성공: {}", result);
        } catch (Exception e) {
            log.warn("FCM 알림 전송 실패", e);
        }
    }

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
        sendMessage(FcmSendRequest.of(targetDeviceToken, title, content, imageUrl, path, schemeUri, type));
    }

    public List<FcmSendResponse> sendMessages(List<FcmSendRequest> requests) {
        List<FcmSendResponse> responses = new ArrayList<>(requests.size());
        for (int start = 0; start < requests.size(); start += FCM_MESSAGE_BATCH_SIZE) {
            int end = Math.min(start + FCM_MESSAGE_BATCH_SIZE, requests.size());
            List<Message> messages = requests.subList(start, end).stream()
                .map(this::buildMessage)
                .toList();

            try {
                BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEach(messages);
                responses.addAll(batchResponse.getResponses().stream()
                    .map(this::toFcmSendResponse)
                    .toList());
            } catch (Exception e) {
                log.warn("FCM 알림 전송 실패. batchSize={}", messages.size(), e);
                for (int i = 0; i < messages.size(); i++) {
                    responses.add(FcmSendResponse.failed(e.getClass().getSimpleName(), null));
                }
            }
        }
        return responses;
    }

    private Message buildMessage(FcmSendRequest request) {
        Map<String, String> chatData = request.roomId() == null ? Map.of() : Map.of(
            "notification_type", "CHAT_MESSAGE", "room_id", request.roomId()
        );
        Map<String, String> data = generateAndroidData(
            request.title(), request.content(), request.imageUrl(), request.schemeUri(), request.type(),
            request.notificationId(), chatData
        );
        Message.Builder message = Message.builder()
            .setToken(request.targetDeviceToken())
            .setApnsConfig(generateAppleConfig(
                request.title(), request.content(), request.imageUrl(), request.path(), request.type(),
                request.schemeUri(), request.notificationId(), chatData
            ))
            .setAndroidConfig(AndroidConfig.builder().putAllData(data).setPriority(HIGH).build());
        if (request.roomId() != null) {
            message.putAllData(chatData);
        }
        return message.build();
    }

    private FcmSendResponse toFcmSendResponse(SendResponse response) {
        if (response.isSuccessful()) {
            return FcmSendResponse.succeeded();
        }
        FirebaseMessagingException exception = response.getException();
        String errorCode = exception.getErrorCode().name();
        String messagingErrorCode = Objects.toString(exception.getMessagingErrorCode(), null);
        return FcmSendResponse.failed(errorCode, messagingErrorCode);
    }

    private ApnsConfig generateAppleConfig(
        String title,
        String content,
        String imageUrl,
        MobileAppPath path,
        String type,
        String schemeUri,
        String notificationId,
        Map<String, String> chatData
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
            .putAllCustomData(appleCustomData(type, schemeUri, notificationId, chatData))
            .build();
    }

    private Map<String, Object> appleCustomData(
        String type,
        String schemeUri,
        String notificationId,
        Map<String, String> chatData
    ) {
        Map<String, Object> customData = new HashMap<>();
        if (notificationId != null) {
            customData.put("notification_id", notificationId);
        }
        if (type != null) {
            customData.put("type", type);
        }
        if (schemeUri != null) {
            customData.put("schemeUri", schemeUri);
        }
        customData.putAll(chatData);
        return customData;
    }

    private Map<String, String> generateAndroidData(
        String title,
        String content,
        String imageUrl,
        String schemeUri,
        String type,
        String notificationId,
        Map<String, String> chatData
    ) {
        Map<String, String> androidNotificationV2 = new HashMap<>();
        if (notificationId != null) {
            androidNotificationV2.put("notification_id", notificationId);
        }
        androidNotificationV2.put("title", title != null ? title : "");
        androidNotificationV2.put("content", content != null ? content : "");
        androidNotificationV2.put("imageUrl", imageUrl != null ? imageUrl : "");
        if (schemeUri != null) {
            androidNotificationV2.put("url", "koin://" + schemeUri);
        }
        androidNotificationV2.put("type", type != null ? type : "");

        androidNotificationV2.putAll(chatData);
        return androidNotificationV2;
    }
}
