package in.koreatech.koin.socket.domain.chatroom.service.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import in.koreatech.koin.socket.domain.chatroom.model.LostItemChatUserBlockEntity;
import in.koreatech.koin.socket.domain.chatroom.repository.LostItemChatUserBlockRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserBlockReader {

    private final LostItemChatUserBlockRepository userBlockRepository;

    public Optional<LostItemChatUserBlockEntity> readByBlockerUserIdAndBlockedUserIdAndIsActive(
        Integer blockerUserId, Integer blockedUserId, Boolean isActive
    ) {
        return userBlockRepository.findByBlockerUserAndBlockedUserAndIsActive(blockerUserId, blockedUserId, isActive);
    }
}
