package in.koreatech.koin.unit.infrastructure.fcm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.SendResponse;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.fcm.FcmSendRequest;
import in.koreatech.koin.infrastructure.fcm.FcmSendResponse;

class FcmClientTest {

    private static final Map<String, String> CHAT_DATA = Map.of(
        "notification_type", "CHAT_MESSAGE", "room_id", "team-recruitment-30-12"
    );

    private final FcmClient client = new FcmClient();

    @Test
    void bulk_serializes_metadata_for_both_platforms_and_preserves_legacy_payload() throws Exception {
        FirebaseMessaging firebase = mock(FirebaseMessaging.class);
        BatchResponse response = mock(BatchResponse.class);
        SendResponse sent = mock(SendResponse.class);
        when(sent.isSuccessful()).thenReturn(true);
        when(response.getResponses()).thenReturn(List.of(sent, sent));
        when(firebase.sendEach(anyList())).thenReturn(response);
        try (MockedStatic<FirebaseMessaging> boundary = mockStatic(FirebaseMessaging.class)) {
            boundary.when(FirebaseMessaging::getInstance).thenReturn(firebase);
            List<FcmSendResponse> responses = client.sendMessages(List.of(
                request().withRoomId("team-recruitment-30-12"),
                FcmSendRequest.of("token", "title", "body", "https://example.com/image.png", MobileAppPath.CHAT,
                    "chat/12", "application_accepted", "90")));
            assertThat(responses).containsExactly(FcmSendResponse.succeeded(), FcmSendResponse.succeeded());
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Message>> captor = ArgumentCaptor.forClass(List.class);
            verify(firebase).sendEach(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
            assertPayload(captor.getValue().get(0), CHAT_DATA, "90", "new_chat_message");
            assertPayload(captor.getValue().get(1), Map.of(), "90", "application_accepted");
        }
    }

    @Test
    void legacy_single_path_keeps_its_payload_without_common_metadata() throws Exception {
        FirebaseMessaging firebase = mock(FirebaseMessaging.class);
        try (MockedStatic<FirebaseMessaging> boundary = mockStatic(FirebaseMessaging.class)) {
            boundary.when(FirebaseMessaging::getInstance).thenReturn(firebase);
            client.sendMessage("token", "title", "body", "https://example.com/image.png", MobileAppPath.CHAT,
                "chat/12", "new_chat_message");
            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(firebase).send(captor.capture());
            assertPayload(captor.getValue(), Map.of(), null, "new_chat_message");
        }
    }

    @Test
    void both_single_paths_skip_blank_tokens_and_swallow_transport_failures() throws Exception {
        FirebaseMessaging firebase = mock(FirebaseMessaging.class);
        try (MockedStatic<FirebaseMessaging> boundary = mockStatic(FirebaseMessaging.class)) {
            boundary.when(FirebaseMessaging::getInstance).thenReturn(firebase);
            for (String token : new String[] {null, "  "}) {
                client.sendMessage(FcmSendRequest.of(token, null, null, null, null, null, null));
                client.sendMessage(token, null, null, null, null, null, null);
            }
            verifyNoInteractions(firebase);
            when(firebase.send(any(Message.class))).thenThrow(new IllegalStateException("test failure"));
            assertThatCode(() -> client.sendMessage(request())).doesNotThrowAnyException();
            assertThatCode(() -> client.sendMessage("token", "title", "body", null, MobileAppPath.CHAT,
                "chat/12", "new_chat_message")).doesNotThrowAnyException();
            verify(firebase, times(2)).send(any(Message.class));
        }
    }

    private FcmSendRequest request() {
        return FcmSendRequest.of("token", "title", "body", "https://example.com/image.png", MobileAppPath.CHAT,
            "chat/12", "new_chat_message", "90");
    }

    @SuppressWarnings("unchecked")
    private void assertPayload(Message message, Map<String, String> chatData, String notificationId, String type) {
        assertThat(ReflectionTestUtils.getField(message, "token")).isEqualTo("token");
        AndroidConfig android = (AndroidConfig)ReflectionTestUtils.getField(message, "androidConfig");
        ApnsConfig apple = (ApnsConfig)ReflectionTestUtils.getField(message, "apnsConfig");
        assertThat(ReflectionTestUtils.getField(android, "priority")).isEqualTo("high");
        Object appleOptions = ReflectionTestUtils.getField(apple, "fcmOptions");
        assertThat(ReflectionTestUtils.getField(appleOptions, "image"))
            .isEqualTo("https://example.com/image.png");
        Map<String, String> data = (Map<String, String>)ReflectionTestUtils.getField(android, "data");
        Map<String, String> rootData = (Map<String, String>)ReflectionTestUtils.getField(message, "data");
        if (chatData.isEmpty()) {
            assertThat(rootData).isNull();
        } else {
            assertThat(rootData).containsExactlyInAnyOrderEntriesOf(chatData);
        }
        assertThat(ReflectionTestUtils.getField(message, "notification")).isNull();
        assertThat(ReflectionTestUtils.getField(message, "webpushConfig")).isNull();
        Map<String, Object> payload = (Map<String, Object>)ReflectionTestUtils.getField(apple, "payload");
        assertThat(data).containsAllEntriesOf(chatData)
            .containsEntry("title", "title").containsEntry("content", "body")
            .containsEntry("imageUrl", "https://example.com/image.png")
            .containsEntry("url", "koin://chat/12").containsEntry("type", type)
            .hasSize(5 + chatData.size() + (notificationId == null ? 0 : 1));
        assertThat(payload).containsAllEntriesOf(chatData)
            .containsEntry("type", type).containsEntry("schemeUri", "chat/12")
            .hasSize(3 + chatData.size() + (notificationId == null ? 0 : 1));
        if (notificationId == null) {
            assertThat(data).doesNotContainKey("notification_id");
            assertThat(payload).doesNotContainKey("notification_id");
        } else {
            assertThat(data).containsEntry("notification_id", notificationId);
            assertThat(payload).containsEntry("notification_id", notificationId);
        }
        Map<String, Object> aps = (Map<String, Object>)payload.get("aps");
        assertThat(aps).containsEntry("category", "chat").containsEntry("sound", "default")
            .containsEntry("mutable-content", 1);
        Object alert = aps.get("alert");
        assertThat(ReflectionTestUtils.getField(alert, "title")).isEqualTo("title");
        assertThat(ReflectionTestUtils.getField(alert, "body")).isEqualTo("body");
    }
}
