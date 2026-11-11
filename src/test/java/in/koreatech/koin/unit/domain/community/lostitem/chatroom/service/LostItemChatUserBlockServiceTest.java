package in.koreatech.koin.unit.domain.community.lostitem.chatroom.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatUserBlockEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatUserBlockRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatUserBlockService;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class LostItemChatUserBlockServiceTest {

    private final LostItemChatRoomInfoRepository rooms = mock(LostItemChatRoomInfoRepository.class);
    private final InMemoryBlocks blocks = new InMemoryBlocks();
    private final LostItemChatAccessService access = new LostItemChatAccessService(rooms, blocks);
    private final LostItemChatUserBlockService service = new LostItemChatUserBlockService(blocks, access);

    @Test
    void eitherParticipantCanMaintainAnIndependentBlock() {
        when(rooms.getByArticleIdAndChatRoomId(10, 1)).thenReturn(LostItemChatRoomInfoEntity.builder()
            .articleId(10).chatRoomId(1).authorId(7).ownerId(8).build());

        service.blockUser(10, 1, 7);
        service.blockUser(10, 1, 8);
        service.blockUser(10, 1, 8);
        service.unBlockUser(10, 1, 7);

        assertThat(blocks.findByBlockerUserAndBlockedUserAndIsActive(8, 7, true)).isPresent();
        assertThat(blocks.findByBlockerUserAndBlockedUserAndIsActive(7, 8, true)).isEmpty();
        assertThat(blocks.entries).hasSize(2);
        assertThatThrownBy(() -> access.validateAccess(10, 1, 7))
            .isInstanceOfSatisfying(CustomException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.FORBIDDEN_BLOCKED_USER));
    }

    @Test
    void outsiderCannotChangeParticipantBlocks() {
        when(rooms.getByArticleIdAndChatRoomId(10, 1)).thenReturn(LostItemChatRoomInfoEntity.builder()
            .articleId(10).chatRoomId(1).authorId(7).ownerId(8).build());
        assertThatThrownBy(() -> service.blockUser(10, 1, 9)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> service.unBlockUser(10, 1, 9)).isInstanceOf(CustomException.class);
        assertThat(blocks.entries).isEmpty();
    }

    private static class InMemoryBlocks implements LostItemChatUserBlockRepository {
        private final Map<String, LostItemChatUserBlockEntity> entries = new HashMap<>();

        @Override
        public LostItemChatUserBlockEntity save(LostItemChatUserBlockEntity entity) {
            entries.put(entity.getBlockerUserId() + ":" + entity.getBlockedUserId(), entity);
            return entity;
        }

        @Override
        public Optional<LostItemChatUserBlockEntity> findByBlockerUserAndBlockedUserAndIsActive(
            Integer blocker, Integer blocked, Boolean active
        ) {
            return Optional.ofNullable(entries.get(blocker + ":" + blocked))
                .filter(entity -> active.equals(entity.getIsActive()));
        }

        @Override
        public void updateIsActiveByBlockerAndBlockedUser(Integer blocker, Integer blocked, Boolean active) {
            if (entries.containsKey(blocker + ":" + blocked)) {
                save(LostItemChatUserBlockEntity.builder().blockerUserId(blocker).blockedUserId(blocked)
                    .isActive(active).build());
            }
        }
    }
}
