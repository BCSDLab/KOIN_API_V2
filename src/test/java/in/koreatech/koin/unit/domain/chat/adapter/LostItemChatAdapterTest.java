package in.koreatech.koin.unit.domain.chat.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.chat.adapter.LostItemChatAdapter;
import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessagePollingService;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatUserBlockRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatUserBlockEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.domain.community.lostitem.chatroom.usecase.LostItemChatRoomUseCase;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class LostItemChatAdapterTest {

    @Mock private LostItemChatRoomInfoRepository rooms;
    @Mock private LostItemChatRoomInfoService roomInfo;
    @Mock private LostItemChatRoomUseCase roomDisplay;
    @Mock private LostItemChatUserBlockRepository blocks;
    @Mock private ChatMessageRedisRepository messages;
    @Mock private ChatMessagePollingService polling;
    @Mock private UserRepository users;
    private LostItemChatAdapter adapter;
    private final ChatRoomKey key = new ChatRoomKey(ChatDomainType.LOST_ITEM, 100, 1);
    private final LocalDateTime time = LocalDateTime.of(2026, 10, 6, 12, 0);
    private static final long TSID = 9007199254740993L;

    @BeforeEach
    void setUp() {
        adapter = new LostItemChatAdapter(roomInfo, roomDisplay,
            new in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService(rooms, blocks),
            messages, polling, users);
    }

    @Test
    void outsiderCannotReadDisplayMessagesOrSendAndCausesNoSideEffects() {
        room();
        assertDenied(30, key);
        verifyNoInteractions(blocks, roomInfo, roomDisplay, messages, polling, users);
    }

    @Test
    void mismatchedRoomBindingIsRejectedBeforeAnyEffects() {
        when(rooms.getByArticleIdAndChatRoomId(100, 1))
            .thenReturn(LostItemChatRoomInfoEntity.toEntity(101, 1, 20, 10));
        assertDenied(20, key);
        verifyNoInteractions(blocks, roomInfo, roomDisplay, messages, polling, users);
    }

    @Test
    void foreignDomainIsRejectedBeforeRepositoryAccess() {
        assertDenied(20, new ChatRoomKey(ChatDomainType.CALLVAN, 100, 1));
        verifyNoInteractions(rooms, blocks, roomInfo, roomDisplay, messages, polling, users);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void eitherDirectionOfBlockingRejectsEveryOperation(boolean blockedByCaller) {
        room();
        int blocker = blockedByCaller ? 20 : 10;
        int blocked = blockedByCaller ? 10 : 20;
        doReturn(Optional.empty()).when(blocks)
            .findByBlockerUserAndBlockedUserAndIsActive(blocked, blocker, true);
        doReturn(Optional.of(LostItemChatUserBlockEntity.builder()
            .blockerUserId(blocker).blockedUserId(blocked).isActive(true).build()))
            .when(blocks).findByBlockerUserAndBlockedUserAndIsActive(blocker, blocked, true);
        assertDenied(20, key);
        verifyNoInteractions(roomInfo, roomDisplay, messages, polling, users);
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 20})
    void commonReadIsPureAndReturnsLatestMessages(int userId) {
        room();
        when(messages.findByArticleIdAndChatRoomId(100, 1)).thenReturn(
            List.of(message(TSID + 2, false), message(TSID, true), message(TSID + 1, false)));

        var result = adapter.getMessages(userId, key, new ChatMessageQuery(null, null, 2));

        assertThat(result).extracting(it -> it.messageId())
            .containsExactly("9007199254740994", "9007199254740995");
        assertThat(result).extracting(it -> it.unreadCount()).containsExactly(1, 1);
        assertThat(result.get(0).userNickname()).isEqualTo("stored nickname");
        assertThat(result.get(0).timestamp()).isEqualTo(time);
        assertThat(result.get(0).content()).isEqualTo("hello");
        assertThat(result.get(0).isImage()).isFalse();
        assertThat(result.get(0).isLeftUser()).isFalse();
        var order = inOrder(blocks, messages);
        order.verify(blocks).findByBlockerUserAndBlockedUserAndIsActive(userId, userId == 10 ? 20 : 10, true);
        order.verify(blocks).findByBlockerUserAndBlockedUserAndIsActive(userId == 10 ? 20 : 10, userId, true);
        verify(messages, never()).markAllMessagesAsRead(anyInt(), anyInt(), anyInt());
        order.verify(messages).findByArticleIdAndChatRoomId(100, 1);
    }

    @Test
    void sendUsesAuthenticatedNicknameAndReturnsPersistedIdAndTime() {
        room();
        User user = mock(User.class);
        when(users.getById(20)).thenReturn(user);
        when(user.getDisplayNickname()).thenReturn("authenticated nickname");
        when(polling.sendMessageEntity(eq(100), eq(1), eq(20), any()))
            .thenReturn(message(TSID, false));

        var response = adapter.sendMessage(20, key, new CreateChatMessageRequest("image url", true));

        ArgumentCaptor<ChatMessage> sent = ArgumentCaptor.forClass(ChatMessage.class);
        verify(polling).sendMessageEntity(eq(100), eq(1), eq(20), sent.capture());
        assertThat(sent.getValue().userId()).isEqualTo(20);
        assertThat(sent.getValue().userNickname()).isEqualTo("authenticated nickname");
        assertThat(sent.getValue().content()).isEqualTo("image url");
        assertThat(sent.getValue().isImage()).isTrue();
        assertThat(response.messageId()).isEqualTo("9007199254740993");
        assertThat(response.timestamp()).isEqualTo(time);
        assertThat(response.unreadCount()).isEqualTo(1);
        verifyNoInteractions(messages);
    }

    private void room() {
        when(rooms.getByArticleIdAndChatRoomId(100, 1))
            .thenReturn(LostItemChatRoomInfoEntity.toEntity(100, 1, 20, 10));
    }

    private ChatMessageEntity message(long id, boolean read) {
        return ChatMessageEntity.builder().id(id).articleId(100).chatRoomId(1).userId(20)
            .nickName("stored nickname").contents("hello").createdAt(time)
            .isRead(read).isImage(false).isDeleted(false).build();
    }

    private void assertDenied(int userId, ChatRoomKey roomKey) {
        assertThatThrownBy(() -> adapter.validateReadAccess(userId, roomKey)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> adapter.markRead(userId, roomKey, TSID)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> adapter.getRoom(userId, roomKey)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> adapter.getMessages(userId, roomKey, new ChatMessageQuery(null, null, 10)))
            .isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> adapter.sendMessage(userId, roomKey, new CreateChatMessageRequest("hello", false)))
            .isInstanceOf(CustomException.class);
    }
}
