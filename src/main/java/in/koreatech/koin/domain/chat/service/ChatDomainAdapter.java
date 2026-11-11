package in.koreatech.koin.domain.chat.service;

import java.util.List;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatRoomResponse;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;

public interface ChatDomainAdapter {

    ChatDomainType domainType();

    void validateReadAccess(Integer userId, ChatRoomKey key);

    void markRead(Integer userId, ChatRoomKey key, Long messageId);

    List<ChatRoomResponse> getRooms(Integer userId, SupportedLanguage language);

    ChatRoomResponse getRoom(Integer userId, ChatRoomKey key, SupportedLanguage language);

    List<ChatMessageResponse> getMessages(Integer userId, ChatRoomKey key, ChatMessageQuery query);

    ChatMessageResponse sendMessage(Integer userId, ChatRoomKey key, CreateChatMessageRequest request);
}
