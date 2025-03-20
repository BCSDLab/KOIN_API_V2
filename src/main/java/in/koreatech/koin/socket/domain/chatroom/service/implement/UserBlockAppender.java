package in.koreatech.koin.socket.domain.chatroom.service.implement;

import org.springframework.stereotype.Component;

import in.koreatech.koin.socket.domain.chatroom.model.LostItemChatUserBlockEntity;
import in.koreatech.koin.socket.domain.chatroom.repository.LostItemChatUserBlockRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserBlockAppender {

    private final LostItemChatUserBlockRepository userBlockRepository;

    public LostItemChatUserBlockEntity save(Integer blockerUserId, Integer blockedUserId) {
        LostItemChatUserBlockEntity entity = LostItemChatUserBlockEntity.builder()
            .blockerUserId(blockerUserId)
            .blockedUserId(blockedUserId)
            .isActive(true)
            .build();

        return userBlockRepository.save(entity);
    }
}
