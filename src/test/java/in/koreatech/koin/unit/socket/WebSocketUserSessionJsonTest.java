package in.koreatech.koin.unit.socket;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import in.koreatech.koin.socket.session.model.WebSocketUserSession;

class WebSocketUserSessionJsonTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void canonicalDeviceTokenRoundTripsAndLegacyDeviceIdIsAccepted() throws Exception {
        String json = mapper.writeValueAsString(WebSocketUserSession.of(1, "push"));
        assertThat(json).contains("\"deviceToken\"").doesNotContain("\"deviceId\"");
        assertThat(mapper.readValue(json, WebSocketUserSession.class).getDeviceToken()).isEqualTo("push");
        assertThat(mapper.readValue(json.replace("deviceToken", "deviceId"), WebSocketUserSession.class)
            .getDeviceToken()).isEqualTo("push");
    }

    @Test
    void connectionDoesNotRequirePushRegistration() throws Exception {
        var session = WebSocketUserSession.of(1, null);
        assertThat(mapper.readValue(mapper.writeValueAsString(session), WebSocketUserSession.class)
            .getUserId()).isEqualTo(1);
    }
}
