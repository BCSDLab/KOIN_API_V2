package in.koreatech.koin.unit.domain.chat.realtime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatRoomPollingSessionRedisRepository;
import in.koreatech.koin.global.exception.CustomException;

class ChatPresenceServiceTest {
    @Test
    void tokens_and_unscoped_sessions_are_never_stored() {
        var redis = mock(StringRedisTemplate.class);
        var presence = new ChatPresenceService(redis, mock(ChatRoomPollingSessionRedisRepository.class),
            mock(in.koreatech.koin.socket.session.service.WebSocketUserSessionService.class));
        var room = new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 2);
        for (String session : new String[] {null, "", "Bearer abc.def.ghi", "abc.def.ghi", "ws:",
            "rest:00000000-0000-0000-0000-000000000001"}) {
            assertThatThrownBy(() -> presence.touch(room, 7, session)).isInstanceOf(CustomException.class);
            assertThatThrownBy(() -> presence.leave(room, 7, session)).isInstanceOf(CustomException.class);
        }
        verifyNoInteractions(redis);
    }

    @Test
    void roomPresenceRejectsInvalidUserIdsBeforeRedisAccess() {
        var redis = mock(StringRedisTemplate.class);
        var presence = new ChatPresenceService(redis, mock(ChatRoomPollingSessionRedisRepository.class),
            mock(in.koreatech.koin.socket.session.service.WebSocketUserSessionService.class));
        var room = new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 2);
        for (Integer userId : new Integer[] {null, 0, -1}) {
            assertThatThrownBy(() -> presence.touch(room, userId, "ws:device"))
                .isInstanceOf(CustomException.class);
            assertThatThrownBy(() -> presence.leave(room, userId, "ws:device"))
                .isInstanceOf(CustomException.class);
            assertThatThrownBy(() -> presence.isOnline(room, userId))
                .isInstanceOf(CustomException.class);
        }
        verifyNoInteractions(redis);
    }
    @Test
    void commonPresenceRecognizesOnlyMatchingLegacyRoom() {
        var redis = mock(StringRedisTemplate.class);
        var legacy = mock(in.koreatech.koin.socket.session.service.WebSocketUserSessionService.class);
        var presence = new ChatPresenceService(redis, mock(ChatRoomPollingSessionRedisRepository.class), legacy);
        var state = in.koreatech.koin.socket.session.model.WebSocketUserSession.of(7, null);
        state.updateStatus(in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM,
            10L, 2L);
        org.mockito.Mockito.when(legacy.read(7)).thenReturn(java.util.Optional.of(state));

        org.assertj.core.api.Assertions.assertThat(presence.isOnline(
            new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 2), 7)).isTrue();
        verifyNoInteractions(redis);
        org.assertj.core.api.Assertions.assertThat(presence.isOnline(
            new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 3), 7)).isFalse();
        state.updateStatus(in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM_LIST,
            -1L, -1L);
        org.assertj.core.api.Assertions.assertThat(presence.isOnline(
            new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 2), 7)).isFalse();
    }

}
