package in.koreatech.koin.global.socket.domain.chatroom.service.implement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.socket.domain.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.global.socket.domain.chatroom.repository.LostItemChatRoomInfoRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomInfoReader {

    private final LostItemChatRoomInfoRepository chatRoomInfoRepository;

    public Optional<LostItemChatRoomInfoEntity> readByArticleIdAndOwnerId(Integer articleId, Integer ownerId) {
        return chatRoomInfoRepository.findByArticleIdAndOwnerId(articleId, ownerId);
    }

    public List<LostItemChatRoomInfoEntity> readByUserId(Integer userId) {
        return chatRoomInfoRepository.findByUserId(userId);
    }

    public Integer getNextChatRoomId(Integer articleId) {
        List<LostItemChatRoomInfoEntity> chatRooms = chatRoomInfoRepository.findByArticleId(articleId);

        if (chatRooms.isEmpty()) {
            return 1;
        }

        return chatRooms.stream()
            .mapToInt(LostItemChatRoomInfoEntity::getChatRoomId)
            .max()
            .orElse(0) + 1;
    }
}
