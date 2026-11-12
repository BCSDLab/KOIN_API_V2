package in.koreatech.koin.unit.domain.translation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatUserBlockService;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.repository.*;
import in.koreatech.koin.domain.translation.model.*;
import in.koreatech.koin.domain.translation.service.*;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.code.ApiResponseCode;
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
    @Mock in.koreatech.koin.domain.community.article.repository.CommentRepository commentRepository;
    @Mock in.koreatech.koin.domain.callvan.repository.CallvanPostRepository callvanPostRepository;
    @Mock in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository callvanParticipantRepository;
    @InjectMocks TranslationSourceReader reader;

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
    void teamNonmemberCannotReadMessage() {
        when(userRepository.getById(7)).thenReturn(mock(User.class));
        var room = mock(TeamRecruitmentChatRoom.class);
        var recruitment = mock(TeamRecruitment.class);
        when(recruitment.getId()).thenReturn(1);
        when(room.getRecruitment()).thenReturn(recruitment);
        when(teamRoomRepository.findById(2)).thenReturn(Optional.of(room));
        assertThatThrownBy(() -> reader.load(TranslationResource.message(TranslationType.RECRUITMENT_MESSAGE, 1, 2, "3"), 7))
            .isInstanceOfSatisfying(CustomException.class, error ->
                assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN));
        verifyNoInteractions(teamMessageRepository);
    }

    @Test
    void articleBodyTranslatesOnlyTextAndKeepsHtmlAndSourceVersion() {
        String html = "<style>.notice{color:red}</style><p class='notice'>모집 안내 <a href='/apply'>신청하기</a><img src='/photo.png' alt='사진'></p><script>doNotTranslate()</script>";
        var article = mock(in.koreatech.koin.domain.community.article.model.Article.class);
        when(articleRepository.getById(10)).thenReturn(article);
        when(article.getContent()).thenReturn("https://static.test/notice.html");
        when(s3Client.isCustomDomainUrl("https://static.test/notice.html")).thenReturn(true);
        when(s3Client.getContentFromCustomDomainUrl("https://static.test/notice.html")).thenReturn(html);
        var source = reader.load(TranslationResource.article(TranslationType.ARTICLE_CONTENT, 10), null);
        assertThat(source.version()).isEqualTo(TranslationSourceVersion.of(TranslationSourceFields.message(html)));
        assertThat(source.texts()).containsOnlyKeys("text_0", "text_1");
        String translated = ArticleHtmlTranslation.restore(html,
            java.util.Map.of("text_0", "Recruitment <img src=x onerror=alert(1)>", "text_1", "Apply"));
        var document = org.jsoup.Jsoup.parse(translated);
        assertThat(document.select("img")).hasSize(1);
        assertThat(document.selectFirst("a").attr("href")).isEqualTo("/apply");
        assertThat(document.selectFirst("img").attr("alt")).isEqualTo("사진");
        assertThat(document.selectFirst("style").data()).isEqualTo(".notice{color:red}");
        assertThat(document.selectFirst("script").data()).isEqualTo("doNotTranslate()");
        assertThat(document.selectFirst("p").text()).contains("<img src=x onerror=alert(1)>");
    }

    @Test
    void commentUsesParentBindingAndExpiredCallvanRequiresParticipation() {
        when(articleRepository.getById(10)).thenReturn(mock(in.koreatech.koin.domain.community.article.model.Article.class));
        when(commentRepository.findByIdAndArticle_Id(3, 10)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reader.load(new TranslationResource(TranslationType.ARTICLE_COMMENT, "3", 10, 0), null))
            .isInstanceOfSatisfying(CustomException.class, error ->
                assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.TRANSLATION_SOURCE_NOT_FOUND));
        var post = mock(in.koreatech.koin.domain.callvan.model.CallvanPost.class);
        when(callvanPostRepository.getById(4)).thenReturn(post);
        when(post.getId()).thenReturn(4);
        when(post.isExpired()).thenReturn(true);
        when(userRepository.getById(7)).thenReturn(mock(User.class));
        assertThatThrownBy(() -> reader.load(TranslationResource.article(TranslationType.CALLVAN_POST, 4), 7))
            .isInstanceOfSatisfying(CustomException.class, error ->
                assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_ARTICLE));
    }
}
