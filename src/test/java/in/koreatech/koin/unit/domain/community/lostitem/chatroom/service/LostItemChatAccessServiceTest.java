package in.koreatech.koin.unit.domain.community.lostitem.chatroom.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatUserBlockEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatUserBlockRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatUserBlockService;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class LostItemChatAccessServiceTest {
    @Mock private LostItemChatRoomInfoRepository rooms;
    @Mock private LostItemChatUserBlockRepository blocks;

    @Test
    void outsidersCannotReadOrChangeAnotherPairsBlockState() {
        when(rooms.getByArticleIdAndChatRoomId(1, 2)).thenReturn(LostItemChatRoomInfoEntity.toEntity(1, 2, 20, 10));
        var access = new LostItemChatAccessService(rooms, blocks);
        var service = new LostItemChatUserBlockService(blocks, access);
        assertThatThrownBy(() -> access.validateAccess(1, 2, 99)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> service.blockUser(1, 2, 99)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> service.unBlockUser(1, 2, 99)).isInstanceOf(CustomException.class);
        verifyNoInteractions(blocks);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void readGuardChecksBothBlockDirections(boolean callerBlocks) {
        when(rooms.getByArticleIdAndChatRoomId(1, 2)).thenReturn(LostItemChatRoomInfoEntity.toEntity(1, 2, 20, 10));
        var activeBlock = Optional.of(LostItemChatUserBlockEntity.builder()
            .blockerUserId(callerBlocks ? 20 : 10).blockedUserId(callerBlocks ? 10 : 20).isActive(true).build());
        doReturn(callerBlocks ? activeBlock : Optional.empty()).when(blocks)
            .findByBlockerUserAndBlockedUserAndIsActive(20, 10, true);
        doReturn(callerBlocks ? Optional.empty() : activeBlock).when(blocks)
            .findByBlockerUserAndBlockedUserAndIsActive(10, 20, true);

        assertThatThrownBy(() -> new LostItemChatAccessService(rooms, blocks).validateAccess(1, 2, 20))
            .isInstanceOfSatisfying(CustomException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.FORBIDDEN_BLOCKED_USER));
        verify(blocks).findByBlockerUserAndBlockedUserAndIsActive(20, 10, true);
        verify(blocks).findByBlockerUserAndBlockedUserAndIsActive(10, 20, true);
    }

    @Test
    void memberCanUnblockOwnBlockWithoutBeingRejectedByReadGuard() {
        when(rooms.getByArticleIdAndChatRoomId(1, 2)).thenReturn(LostItemChatRoomInfoEntity.toEntity(1, 2, 20, 10));
        var access = new LostItemChatAccessService(rooms, blocks);
        new LostItemChatUserBlockService(blocks, access).unBlockUser(1, 2, 20);
        verify(blocks).updateIsActiveByBlockerAndBlockedUser(20, 10, false);
    }
}
