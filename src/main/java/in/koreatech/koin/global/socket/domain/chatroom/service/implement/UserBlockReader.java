package in.koreatech.koin.global.socket.domain.chatroom.service.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.socket.domain.chatroom.model.LostItemChatUserBlockEntity;
import in.koreatech.koin.global.socket.domain.chatroom.repository.LostItemChatUserBlockRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserBlockReader {

    private final LostItemChatUserBlockRepository userBlockRepository;

    public Optional<LostItemChatUserBlockEntity> readByBlockerUserIdAndBlockedUserId(
        Integer blockerUserId, Integer blockedUserId
    ) {
        return userBlockRepository.findByBlockerUserAndBlockedUser(blockerUserId, blockedUserId);
    }
}
