package in.koreatech.koin.global.socket.domain.chatroom.service.implement;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.socket.domain.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.global.socket.domain.chatroom.repository.LostItemChatRoomInfoRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomInfoAppender {

    private final LostItemChatRoomInfoRepository chatRoomInfoRepository;

    public LostItemChatRoomInfoEntity save(
        Integer articleId,
        Integer chatRoomId,
        Integer ownerId,
        Integer authorId
    ) {
        LostItemChatRoomInfoEntity newInfo = LostItemChatRoomInfoEntity.builder()
            .articleId(articleId)
            .chatRoomId(chatRoomId)
            .ownerId(ownerId)
            .authorId(authorId)
            .build();

       return chatRoomInfoRepository.save(newInfo);
    }

}
