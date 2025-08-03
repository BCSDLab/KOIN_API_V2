package in.koreatech.koin.unit.domain.community.lostitem.chatroom.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixutre.LostItemArticleFixture;
import in.koreatech.koin.unit.fixutre.LostItemChatFixture;
import in.koreatech.koin.unit.fixutre.UserFixture;

@ExtendWith(MockitoExtension.class)
public class LostItemChatRoomInfoServiceTest {

    @InjectMocks
    private LostItemChatRoomInfoService lostItemChatRoomInfoService;
    @Mock
    private LostItemArticleRepository lostItemArticleRepository;
    @Mock
    private LostItemChatRoomInfoRepository chatRoomInfoRepository;
    private User author;
    private User messageSender;
    private LostItemArticle lostItemArticle;
    private final Integer authorId = 1;
    private final Integer messageSenderId = 2;
    private final Integer articleId = 1001;
    private final Integer lostItemArticleId = 101;

    @BeforeEach
    void setUp() {
        author = UserFixture.코인_유저();
        ReflectionTestUtils.setField(author, "id", authorId);

        messageSender = UserFixture.코인_유저();
        ReflectionTestUtils.setField(messageSender, "id", messageSenderId);

        lostItemArticle = LostItemArticleFixture.분실물_게시글_학생등록(articleId, lostItemArticleId, author);
    }

    @Nested
    @DisplayName("채팅방 생성 및 조회 - 성공")
    class GetOrCreateChatRoomSuccess {

        @Test
        void 기존에_생성된_채팅방이_있으면_해당_채팅방_ID를_반환한다() {
            LostItemChatRoomInfoEntity lostItemChatRoomInfo = LostItemChatFixture.분실물_게시글_채팅방(articleId, 77, authorId, messageSenderId);
            when(chatRoomInfoRepository.findByArticleIdAndMessageSenderId(articleId, messageSenderId)).thenReturn(
                Optional.of(lostItemChatRoomInfo));

            // when
            Integer chatRoomId = lostItemChatRoomInfoService.getOrCreateChatRoomId(articleId, messageSenderId);

            // then
            assertThat(chatRoomId).isEqualTo(77);
            verify(chatRoomInfoRepository, never()).save(any(LostItemChatRoomInfoEntity.class));
        }

        @Test
        void 첫_번째_채팅방_생성시_채팅방_ID가_1로_생성된다() {
            // given
            when(chatRoomInfoRepository.findByArticleIdAndMessageSenderId(articleId, messageSenderId)).thenReturn(
                Optional.empty());
            when(lostItemArticleRepository.getByArticleId(articleId)).thenReturn(lostItemArticle);
            when(chatRoomInfoRepository.findByArticleId(articleId)).thenReturn(List.of());

            // when
            Integer chatRoomId = lostItemChatRoomInfoService.getOrCreateChatRoomId(articleId, messageSenderId);

            // then
            assertThat(chatRoomId).isEqualTo(1);
            verify(chatRoomInfoRepository).save(argThat(entity ->
                entity.getArticleId().equals(articleId) &&
                    entity.getChatRoomId().equals(1) &&
                    entity.getOwnerId().equals(messageSenderId) &&
                    entity.getAuthorId().equals(authorId)
            ));
        }

        @Test
        void 기존_채팅방들이_있으면_최대_ID에_1_더한_값으로_새_채팅방을_생성한다() {
            when(chatRoomInfoRepository.findByArticleIdAndMessageSenderId(articleId, messageSenderId)).thenReturn(
                Optional.empty());
            when(lostItemArticleRepository.getByArticleId(articleId)).thenReturn(lostItemArticle);
            LostItemChatRoomInfoEntity lostItemChatRoomInfoA = LostItemChatFixture.분실물_게시글_채팅방(articleId, 1, authorId, 888);
            LostItemChatRoomInfoEntity lostItemChatRoomInfoB = LostItemChatFixture.분실물_게시글_채팅방(articleId, 2, authorId, 999);
            when(chatRoomInfoRepository.findByArticleId(articleId)).thenReturn(List.of(lostItemChatRoomInfoA, lostItemChatRoomInfoB));

            // when
            Integer chatRoomId = lostItemChatRoomInfoService.getOrCreateChatRoomId(articleId, messageSenderId);

            // then
            assertThat(chatRoomId).isEqualTo(3);
            verify(chatRoomInfoRepository).save(argThat(entity ->
                entity.getChatRoomId().equals(3)
            ));
        }
    }

    @Nested
    @DisplayName("채팅방 생성 - 실패")
    class GetOrCreateChatRoomFailure {

        @Test
        void 게시글_작성자가_자신의_게시글에_채팅방을_생성하려_하면_예외가_발생한다() {
            // given
            when(chatRoomInfoRepository.findByArticleIdAndMessageSenderId(articleId, authorId)).thenReturn(
                Optional.empty());
            when(lostItemArticleRepository.getByArticleId(articleId)).thenReturn(lostItemArticle);

            // when & then
            assertCustomExceptionThrown(
                () -> lostItemChatRoomInfoService.getOrCreateChatRoomId(articleId, authorId),
                ApiResponseCode.INVALID_SELF_CHAT
            );

            verify(chatRoomInfoRepository, never()).save(any(LostItemChatRoomInfoEntity.class));
        }

        @Test
        void 탈퇴한_사용자의_게시글에_채팅방을_생성하려_하면_예외가_발생한다() {
            // given
            LostItemArticle articleWithNullAuthor = LostItemArticleFixture.분실물_게시글_학생등록(articleId, lostItemArticleId, null);
            when(chatRoomInfoRepository.findByArticleIdAndMessageSenderId(articleId, authorId)).thenReturn(
                Optional.empty());
            when(lostItemArticleRepository.getByArticleId(articleId)).thenReturn(articleWithNullAuthor);


            // when & then
            assertCustomExceptionThrown(
                () -> lostItemChatRoomInfoService.getOrCreateChatRoomId(articleId, authorId),
                ApiResponseCode.NOT_FOUND_USER
            );

            verify(chatRoomInfoRepository, never()).save(any(LostItemChatRoomInfoEntity.class));
        }
    }

    private void assertCustomExceptionThrown(Runnable runnable, ApiResponseCode expectedCode) {
        CustomException exception = assertThrows(CustomException.class, runnable::run);
        assertThat(exception.getErrorCode()).isEqualTo(expectedCode);
    }
}
