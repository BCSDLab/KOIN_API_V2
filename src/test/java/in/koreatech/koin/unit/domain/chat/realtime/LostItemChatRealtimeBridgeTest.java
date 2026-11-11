package in.koreatech.koin.unit.domain.chat.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatRealtimeEvent;
import in.koreatech.koin.domain.chat.event.ChatRealtimeReceivedEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.realtime.LostItemChatRealtimeBridge;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class LostItemChatRealtimeBridgeTest {
    private final SimpMessageSendingOperations broker = mock(SimpMessageSendingOperations.class);
    private final LostItemChatRoomInfoRepository rooms = mock(LostItemChatRoomInfoRepository.class);
    private final LostItemChatRoomInfoService roomService = mock(LostItemChatRoomInfoService.class);
    private final ChatService chatService = mock(ChatService.class);
    private final LostItemChatRealtimeBridge bridge = new LostItemChatRealtimeBridge(broker, rooms, roomService, chatService);
    private final ChatRoomKey room = new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 2);

    private final Logger logger = (Logger)LoggerFactory.getLogger(LostItemChatRealtimeBridge.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private Level previousLevel;

    @BeforeEach
    void captureLogs() {
        previousLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void restoreLogger() {
        logger.detachAppender(logs);
        logger.setLevel(previousLevel);
        logs.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"access-server-error", "broker"})
    void listFailuresAreSafelyObservableAndDoNotPreventNextRecipient(String stage) {
        String privateDetail = "private payload Bearer secret-token";
        RuntimeException failure = stage.equals("access-server-error")
            ? CustomException.of(ApiResponseCode.INTERNAL_SERVER_ERROR, privateDetail,
                new IllegalStateException(privateDetail))
            : new IllegalStateException(privateDetail);
        failure.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("example.ChatStore", "refresh", "ChatStore.java", 123)
        });
        when(rooms.getByArticleIdAndChatRoomId(10, 2)).thenReturn(LostItemChatRoomInfoEntity.builder()
            .articleId(10).chatRoomId(2).authorId(7).ownerId(8).build());
        when(roomService.getAllChatRoomInfo(8)).thenReturn(List.of());
        switch (stage) {
            case "access-server-error" ->
                when(chatService.validateReadAccess(7, room.encoded())).thenAnswer(invocation -> { throw failure; });
            case "broker" -> {
                when(roomService.getAllChatRoomInfo(7)).thenReturn(List.of());
                doAnswer(invocation -> { throw failure; })
                    .when(broker).convertAndSend("/topic/chatroom/list/7", List.of());
            }
            default -> throw new AssertionError(stage);
        }

        assertThatCode(() -> bridge.onReceived(new ChatRealtimeReceivedEvent(new ChatRealtimeEvent(
            ChatRealtimeEvent.Type.ROOM_CHANGED, room.encoded(), null), true))).doesNotThrowAnyException();

        verify(broker).convertAndSend("/topic/chatroom/list/8", List.of());
        if (!stage.equals("broker")) {
            verify(broker, never()).convertAndSend(eq("/topic/chatroom/list/7"), any(Object.class));
        }
        assertThat(logs.list).anySatisfy(event -> {
            assertThat(event.getLevel().isGreaterOrEqual(Level.WARN)).isTrue();
            assertThat(event.getFormattedMessage()).contains(failure.getClass().getSimpleName(),
                "ChatStore.java", "123");
        });
        assertThat(logs.list).allSatisfy(event -> {
            assertThat(event.getFormattedMessage()).doesNotContain(failure.getMessage(), privateDetail, "secret-token");
            assertThat(event.getThrowableProxy()).isNull();
        });
    }

    @ParameterizedTest
    @EnumSource(value = ApiResponseCode.class, names = {
        "UNAUTHORIZED_USER", "FORBIDDEN_AUTHOR", "NOT_FOUND_LOST_ITEM_CHATROOM"
    })
    void expectedAccessSkipsStayAtDebugAndDoNotPreventNextRecipient(ApiResponseCode code) {
        when(rooms.getByArticleIdAndChatRoomId(10, 2)).thenReturn(LostItemChatRoomInfoEntity.builder()
            .articleId(10).chatRoomId(2).authorId(7).ownerId(8).build());
        when(chatService.validateReadAccess(7, room.encoded()))
            .thenThrow(CustomException.of(code, "private payload Bearer secret-token"));
        when(roomService.getAllChatRoomInfo(8)).thenReturn(List.of());

        assertThatCode(() -> bridge.onReceived(new ChatRealtimeReceivedEvent(new ChatRealtimeEvent(
            ChatRealtimeEvent.Type.ROOM_CHANGED, room.encoded(), null), true))).doesNotThrowAnyException();

        verify(roomService, never()).getAllChatRoomInfo(7);
        verify(broker).convertAndSend("/topic/chatroom/list/8", List.of());
        verifyNoMoreInteractions(broker);
        assertThat(logs.list).isNotEmpty().allSatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
            assertThat(event.getFormattedMessage()).contains("CustomException")
                .doesNotContain("private payload", "secret-token");
            assertThat(event.getThrowableProxy()).isNull();
        });
    }

    @Test
    void relays_canonical_fields_and_refreshes_only_current_authorized_members() {
        when(rooms.getByArticleIdAndChatRoomId(10, 2)).thenReturn(LostItemChatRoomInfoEntity.builder()
            .articleId(10).chatRoomId(2).authorId(7).ownerId(8).build());
        when(chatService.validateReadAccess(7, room.encoded())).thenReturn(room);
        when(chatService.validateReadAccess(8, room.encoded()))
            .thenThrow(CustomException.of(ApiResponseCode.FORBIDDEN_AUTHOR));
        when(roomService.getAllChatRoomInfo(7)).thenReturn(List.of());
        var message = new ChatMessageResponse("123", 7, "canonical sender", "saved content",
            LocalDateTime.of(2026, 10, 12, 12, 0), false, 1, false);
        bridge.onReceived(new ChatRealtimeReceivedEvent(
            new ChatRealtimeEvent(ChatRealtimeEvent.Type.MESSAGE_CREATED, room.encoded(), message), true));
        verify(broker).convertAndSend("/topic/chat/10/2",
            new in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse(
                7, message.userNickname(), message.content(), message.timestamp(), false));
        verify(chatService, never()).markRead(anyInt(), anyString(), anyLong());
        verify(broker).convertAndSend("/topic/chatroom/list/7", List.of());
        verifyNoMoreInteractions(broker);
        verify(roomService, never()).getAllChatRoomInfo(8);
    }

    @Test
    void legacyMessagesDoNotBroadcastTwiceOrRefreshBothLists() {
        var message = new ChatMessageResponse("123", 7, "sender", "saved", LocalDateTime.now(), false, 1, false);
        bridge.onReceived(new ChatRealtimeReceivedEvent(new ChatRealtimeEvent(
            ChatRealtimeEvent.Type.MESSAGE_CREATED, room.encoded(), message), false));
        verifyNoInteractions(broker, rooms, roomService, chatService);
    }

    @Test
    void other_domains_never_enter_the_legacy_bridge() {
        bridge.onReceived(new ChatRealtimeReceivedEvent(new ChatRealtimeEvent(
            ChatRealtimeEvent.Type.ROOM_CHANGED, "callvan-10-2", null)));
        verifyNoInteractions(broker, rooms, roomService, chatService);
    }

}
