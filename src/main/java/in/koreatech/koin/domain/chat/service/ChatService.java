package in.koreatech.koin.domain.chat.service;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatRoomResponse;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@Service
public class ChatService {

    private final Map<ChatDomainType, ChatDomainAdapter> adapters = new EnumMap<>(ChatDomainType.class);
    private final UserRepository userRepository;
    private final ChatPresenceService presenceService;

    public ChatService(List<ChatDomainAdapter> domainAdapters, UserRepository userRepository,
        ChatPresenceService presenceService) {
        this.userRepository = userRepository;
        this.presenceService = presenceService;
        for (ChatDomainAdapter adapter : domainAdapters) {
            if (adapters.put(adapter.domainType(), adapter) != null) {
                throw new IllegalStateException("Duplicate chat adapter: " + adapter.domainType());
            }
        }
    }

    public List<ChatRoomResponse> getRooms(Integer userId, ChatDomainType domainType) {
        User user = getUser(userId);
        if (domainType != null) {
            domainType.authorize(user);
            return sortRooms(adapter(domainType).getRooms(userId));
        }
        return sortRooms(adapters.entrySet().stream()
            .filter(entry -> entry.getKey().supports(user))
            .flatMap(entry -> {
                entry.getKey().authorize(user);
                return entry.getValue().getRooms(userId).stream();
            })
            .toList());
    }

    public ChatRoomResponse getRoom(Integer userId, String roomId) {
        ChatRoomKey key = authorize(userId, roomId);
        return adapter(key.domainType()).getRoom(userId, key);
    }

    public List<ChatMessageResponse> getMessages(Integer userId, String roomId, ChatMessageQuery query) {
        ChatRoomKey key = authorize(userId, roomId);
        return adapter(key.domainType()).getMessages(userId, key, query);
    }

    public ChatMessageResponse sendMessage(Integer userId, String roomId, CreateChatMessageRequest request) {
        ChatRoomKey key = authorize(userId, roomId);
        return adapter(key.domainType()).sendMessage(userId, key, request);
    }

    public ChatRoomKey validateReadAccess(Integer userId, String roomId) {
        ChatRoomKey key = authorize(userId, roomId);
        adapter(key.domainType()).validateReadAccess(userId, key);
        return key;
    }

    public void markRead(Integer userId, String roomId, Long messageId) {
        ChatRoomKey key = authorize(userId, roomId);
        if (messageId == null || messageId < 1) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
        adapter(key.domainType()).markRead(userId, key, messageId);
    }

    public void touchPresence(Integer userId, String roomId) {
        ChatRoomKey key = validateReadAccess(userId, roomId);
        presenceService.touchPolling(key, userId);
    }

    public void leavePresence(Integer userId, String roomId) {
        getUser(userId);
        presenceService.leavePolling(ChatRoomKey.parse(roomId), userId);
    }

    private ChatRoomKey authorize(Integer userId, String roomId) {
        ChatRoomKey key = ChatRoomKey.parse(roomId);
        key.domainType().authorize(getUser(userId));
        return key;
    }

    private User getUser(Integer userId) {
        if (userId == null || userId < 1) {
            throw AuthenticationException.withDetail("채팅 API는 로그인이 필요합니다.");
        }
        return userRepository.getById(userId);
    }

    private ChatDomainAdapter adapter(ChatDomainType domainType) {
        ChatDomainAdapter adapter = adapters.get(domainType);
        if (adapter == null) {
            throw new IllegalStateException("Missing chat adapter: " + domainType);
        }
        return adapter;
    }

    private List<ChatRoomResponse> sortRooms(List<ChatRoomResponse> rooms) {
        return rooms.stream()
            .sorted(Comparator.comparing(
                    (ChatRoomResponse room) -> room.lastMessageAt() == null ? null
                        : room.lastMessageAt().truncatedTo(ChronoUnit.SECONDS),
                    Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ChatRoomResponse::roomId))
            .toList();
    }
}
