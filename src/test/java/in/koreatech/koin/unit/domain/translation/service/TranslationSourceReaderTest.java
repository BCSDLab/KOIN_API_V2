package in.koreatech.koin.unit.domain.translation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.community.article.dto.ArticleAiSummaryResponse;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryView;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatUserBlockService;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.repository.*;
import in.koreatech.koin.domain.translation.model.*;
import in.koreatech.koin.domain.translation.service.*;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.infrastructure.s3.client.S3ClientWrapper;

@ExtendWith(MockitoExtension.class)
class TranslationSourceReaderTest {
    @Mock ArticleRepository articleRepository;
    @Mock LostItemArticleRepository lostItemArticleRepository;
    @Mock ArticleAiSummaryService summaryService;
    @Mock S3ClientWrapper s3Client;
    @Mock TeamRecruitmentRepository recruitmentRepository;
    @Mock LostItemChatRoomInfoRepository lostItemRoomRepository;
    @Mock LostItemChatUserBlockService blockService;
    @Mock ChatMessageRedisRepository lostItemMessageRepository;
    @Mock TeamRecruitmentChatRoomRepository teamRoomRepository;
    @Mock TeamRecruitmentChatMemberRepository teamMemberRepository;
    @Mock TeamRecruitmentChatMessageRepository teamMessageRepository;
    @Mock UserRepository userRepository;
    @InjectMocks TranslationSourceReader reader;

    @Test
    void anonymousCannotReadEitherPrivateMessage() {
        for (var type : List.of(TranslationType.LOST_ITEM_MESSAGE, TranslationType.RECRUITMENT_MESSAGE)) {
            assertThatThrownBy(() -> reader.load(TranslationResource.message(type, 1, 2, "3"), null))
                .isInstanceOf(AuthenticationException.class);
        }
        verifyNoInteractions(lostItemMessageRepository, teamMessageRepository);
    }

    @Test
    void lostItemNonmemberRejectedBeforeBlockOrMessageLookup() {
        when(userRepository.getById(7)).thenReturn(mock(User.class));
        var room = mock(LostItemChatRoomInfoEntity.class);
        when(room.getAuthorId()).thenReturn(1);
        when(room.getOwnerId()).thenReturn(2);
        when(lostItemRoomRepository.getByArticleIdAndChatRoomId(1, 2)).thenReturn(room);
        assertThatThrownBy(() -> reader.load(TranslationResource.message(TranslationType.LOST_ITEM_MESSAGE, 1, 2, "3"), 7))
            .isInstanceOf(AuthorizationException.class);
        verifyNoInteractions(blockService, lostItemMessageRepository);
    }

    @Test
    void wrongTeamParentRejectedBeforeMembershipAndMessageLookup() {
        when(userRepository.getById(7)).thenReturn(mock(User.class));
        var room = mock(TeamRecruitmentChatRoom.class);
        var recruitment = mock(TeamRecruitment.class);
        when(recruitment.getId()).thenReturn(9);
        when(room.getRecruitment()).thenReturn(recruitment);
        when(teamRoomRepository.findById(2)).thenReturn(Optional.of(room));
        assertThatThrownBy(() -> reader.load(TranslationResource.message(TranslationType.RECRUITMENT_MESSAGE, 1, 2, "3"), 7))
            .isInstanceOf(CustomException.class);
        verifyNoInteractions(teamMemberRepository, teamMessageRepository);
    }

    @Test
    void historicalTeamMessageDoesNotCheckRecruitmentStatusOrSender() {
        when(userRepository.getById(7)).thenReturn(mock(User.class));
        var room = mock(TeamRecruitmentChatRoom.class);
        var recruitment = mock(TeamRecruitment.class);
        when(recruitment.getId()).thenReturn(1);
        when(room.getRecruitment()).thenReturn(recruitment);
        when(teamRoomRepository.findById(2)).thenReturn(Optional.of(room));
        when(teamMemberRepository.existsByChatRoom_IdAndUser_Id(2, 7)).thenReturn(true);
        var message = mock(TeamRecruitmentChatMessage.class);
        when(message.getContent()).thenReturn("이전 대화입니다");
        when(teamMessageRepository.findByIdAndChatRoom_Id(3, 2)).thenReturn(Optional.of(message));
        var source = reader.load(TranslationResource.message(TranslationType.RECRUITMENT_MESSAGE, 1, 2, "3"), 7);
        assertThat(source.texts()).containsEntry("content", "이전 대화입니다");
        verify(recruitment, never()).isDeleted();
        verifyNoInteractions(recruitmentRepository);
    }

    @Test
    void summaryVersionMatchesExactlyDisplayedItemsAndDoesNotRequestGeneration() {
        var article = mock(Article.class);
        when(articleRepository.getById(1)).thenReturn(article);
        when(article.getContent()).thenReturn("원문");
        var summary = ArticleSummaryView.success(List.of("📅 접수 마감", "invalid line", "📌 신청 조건"));
        when(summaryService.readReadySummary(article, "원문")).thenReturn(summary);
        var source = reader.load(TranslationResource.article(TranslationType.ARTICLE_SUMMARY, 1), null);
        assertThat(source.version()).isEqualTo(ArticleAiSummaryResponse.from(summary).getTranslationSourceVersion());
        assertThat(source.texts()).containsEntry("item_0", "접수 마감");
        verify(summaryService, never()).getSummary(any(), any());
    }

    @Test
    void savedLostItemIdIsStringAndReadStateDoesNotChangeVersion() {
        var message = ChatMessageEntity.builder().id(733900000000012345L).contents("안녕하세요")
            .isImage(false).isRead(false).build();
        var first = ChatMessageResponse.toResponse(message);
        message.readMessage();
        var second = ChatMessageResponse.toResponse(message);
        assertThat(first.messageId()).isEqualTo("733900000000012345");
        assertThat(first.getTranslationSourceVersion()).isEqualTo(second.getTranslationSourceVersion());
        assertThat(first.getTranslationSourceVersion()).isEqualTo(
            TranslationSourceVersion.of(TranslationSourceFields.message("안녕하세요")));
    }

    @Test
    void blockedLostItemMemberCannotReadSource() {
        when(userRepository.getById(7)).thenReturn(mock(User.class));
        var room = mock(LostItemChatRoomInfoEntity.class);
        when(room.getAuthorId()).thenReturn(7);
        when(lostItemRoomRepository.getByArticleIdAndChatRoomId(1, 2)).thenReturn(room);
        doThrow(AuthorizationException.withDetail("blocked")).when(blockService).validateBlockState(1, 2, 7);
        assertThatThrownBy(() -> reader.load(TranslationResource.message(TranslationType.LOST_ITEM_MESSAGE, 1, 2, "3"), 7))
            .isInstanceOf(AuthorizationException.class);
        verifyNoInteractions(articleRepository, lostItemMessageRepository);
    }

    @Test
    void teamNonmemberCannotReadMessage() {
        when(userRepository.getById(7)).thenReturn(mock(User.class));
        var room = mock(TeamRecruitmentChatRoom.class);
        var recruitment = mock(TeamRecruitment.class);
        when(recruitment.getId()).thenReturn(1);
        when(room.getRecruitment()).thenReturn(recruitment);
        when(teamRoomRepository.findById(2)).thenReturn(Optional.of(room));
        assertThatThrownBy(() -> reader.load(TranslationResource.message(TranslationType.RECRUITMENT_MESSAGE, 1, 2, "3"), 7))
            .isInstanceOf(CustomException.class);
        verifyNoInteractions(teamMessageRepository);
    }

    @Test
    void emojiOnlyRecruitmentIsUnsupportedAndNeverSentToAi() {
        var recruitment = mock(TeamRecruitment.class);
        when(recruitmentRepository.findById(1)).thenReturn(Optional.of(recruitment));
        when(recruitment.getTitle()).thenReturn("😀 123");
        var source = reader.load(TranslationResource.article(TranslationType.RECRUITMENT, 1), null);
        assertThat(source.unavailableReason()).isEqualTo(TranslationReason.UNSUPPORTED_CONTENT);
        assertThat(source.texts()).isEmpty();
        assertThat(source.data()).containsEntry("title", "😀 123");
    }

    @Test
    void deletedRecruitmentCannotBeTranslated() {
        var recruitment = mock(TeamRecruitment.class);
        when(recruitmentRepository.findById(1)).thenReturn(Optional.of(recruitment));
        when(recruitment.isDeleted()).thenReturn(true);
        assertThatThrownBy(() -> reader.load(TranslationResource.article(TranslationType.RECRUITMENT, 1), null))
            .isInstanceOf(CustomException.class);
    }

    @Test
    void lostItemSaveReturnsTheExactPersistedMessage() {
        var repository = mock(ChatMessageRedisRepository.class);
        var service = new in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessageService(repository);
        var incoming = new in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage(
            999, "닉네임", "안녕하세요", java.time.LocalDateTime.of(2020, 1, 1, 0, 0), false);
        var saved = service.saveMessage(1, 2, 7, 1, incoming);
        verify(repository).save(same(saved));
        assertThat(saved.getUserId()).isEqualTo(7);
        assertThat(saved.getId()).isNotNull();
        assertThat(ChatMessageResponse.toResponse(saved).messageId()).isEqualTo(saved.getId().toString());
        assertThat(ChatMessageResponse.toResponse(saved).timestamp()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void imageMessagesHaveNoSourceVersion() {
        var message = ChatMessageEntity.builder().id(1L).contents("https://image").isImage(true).build();
        assertThat(ChatMessageResponse.toResponse(message).getTranslationSourceVersion()).isNull();
    }

    @Test
    void nullableQualificationIsPreserved() {
        var recruitment = mock(TeamRecruitment.class);
        when(recruitmentRepository.findById(1)).thenReturn(Optional.of(recruitment));
        when(recruitment.getTitle()).thenReturn("모집");
        var source = reader.load(TranslationResource.article(TranslationType.RECRUITMENT, 1), null);
        assertThat(source.data()).containsEntry("qualification", null).containsEntry("description", null);
        assertThat(source.texts()).containsOnlyKeys("title");
    }
}
