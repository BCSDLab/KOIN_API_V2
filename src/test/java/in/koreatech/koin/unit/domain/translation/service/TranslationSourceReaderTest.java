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
}
