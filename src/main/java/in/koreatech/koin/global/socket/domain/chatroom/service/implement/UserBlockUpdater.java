package in.koreatech.koin.global.socket.domain.chatroom.service.implement;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.socket.domain.chatroom.repository.LostItemChatUserBlockRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserBlockUpdater {

    private final LostItemChatUserBlockRepository userBlockRepository;

    public void block(Integer blockerUserId, Integer blockedUserId) {
        userBlockRepository.updateIsActiveByBlockerAndBlockedUser(blockerUserId, blockedUserId, true);
    }

    public void unBlock(Integer blockerUserId, Integer blockedUserId) {
        userBlockRepository.updateIsActiveByBlockerAndBlockedUser(blockerUserId, blockedUserId, false);
    }
}
