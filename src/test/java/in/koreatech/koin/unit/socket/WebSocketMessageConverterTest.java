package in.koreatech.koin.unit.socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatRealtimeEvent;
import in.koreatech.koin.socket.config.WebSocketConfig;
import in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor;
import in.koreatech.koin.socket.interceptor.ChatOutboundInterceptor;
import in.koreatech.koin.socket.interceptor.ConnectInterceptor;

class WebSocketMessageConverterTest {

    private final ObjectMapper applicationMapper = new ObjectMapper().registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private CompositeMessageConverter converter;

    @BeforeEach
    void setUp() {
        var config = new WebSocketConfig(mock(ConnectInterceptor.class), mock(ChatOutboundInterceptor.class),
            mock(ChatHandshakeInterceptor.class), mock(ThreadPoolTaskScheduler.class), applicationMapper,
            mock(in.koreatech.koin.socket.session.service.ChatSocketSessionRegistry.class));
        var converters = new ArrayList<MessageConverter>();
        assertThat(config.configureMessageConverters(converters)).isFalse();
        converter = new CompositeMessageConverter(converters);
    }

    @Test
    void commonAndLegacyEventsUseTheSameIsoTimestampAsHttp() throws Exception {
        var timestamp = LocalDateTime.of(2026, 10, 12, 13, 14, 15, 123456000);
        var common = new ChatRealtimeEvent(ChatRealtimeEvent.Type.MESSAGE_CREATED, "lost-item-1-2",
            new ChatMessageResponse("123", 7, "user", "hello", timestamp, false, 1, false));
        var legacy = new in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse(
            7, "user", "hello", timestamp, false);
        for (Object event : new Object[] {common, legacy}) {
            var message = converter.toMessage(event, new MessageHeaders(Map.of()));
            assertThat(message).isNotNull();
            var actual = applicationMapper.readTree((byte[])message.getPayload());
            assertThat(actual).isEqualTo(applicationMapper.valueToTree(event));
            var time = actual.has("message") ? actual.path("message").path("timestamp") : actual.path("timestamp");
            assertThat(time.isTextual()).isTrue();
            assertThat(time.textValue()).isEqualTo("2026-10-12T13:14:15.123456");
        }
    }

}
