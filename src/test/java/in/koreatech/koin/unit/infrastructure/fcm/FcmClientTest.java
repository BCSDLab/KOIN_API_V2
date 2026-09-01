package in.koreatech.koin.unit.infrastructure.fcm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;

import in.koreatech.koin.infrastructure.fcm.FcmClient;

class FcmClientTest {

    private final FcmClient fcmClient = new FcmClient();

    @Test
    void notification_id를_Android와_APNs_custom_data에_문자열로_추가한다() {
        AndroidConfig androidConfig = ReflectionTestUtils.invokeMethod(
            fcmClient,
            "generateAndroidConfig",
            "제목",
            "내용",
            null,
            null,
            "application_accepted",
            "90"
        );
        ApnsConfig apnsConfig = ReflectionTestUtils.invokeMethod(
            fcmClient,
            "generateAppleConfig",
            "제목",
            "내용",
            null,
            null,
            "application_accepted",
            null,
            "90"
        );

        assertThat(data(androidConfig)).containsEntry("notification_id", "90");
        assertThat(payload(apnsConfig)).containsEntry("notification_id", "90");
    }

    @Test
    void notification_id가_없으면_custom_data에_추가하지_않는다() {
        AndroidConfig androidConfig = ReflectionTestUtils.invokeMethod(
            fcmClient,
            "generateAndroidConfig",
            "제목",
            "내용",
            null,
            null,
            "application_rejected",
            null
        );
        ApnsConfig apnsConfig = ReflectionTestUtils.invokeMethod(
            fcmClient,
            "generateAppleConfig",
            "제목",
            "내용",
            null,
            null,
            "application_rejected",
            null,
            null
        );

        assertThat(data(androidConfig)).doesNotContainKey("notification_id");
        assertThat(payload(apnsConfig)).doesNotContainKey("notification_id");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> data(AndroidConfig config) {
        return (Map<String, String>)ReflectionTestUtils.getField(config, "data");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> payload(ApnsConfig config) {
        return (Map<String, Object>)ReflectionTestUtils.getField(config, "payload");
    }
}
