package in.koreatech.koin.domain.translation.service;

import static in.koreatech.koin.domain.community.article.service.ArticleService.LOST_ITEM_BOARD_ID;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.dto.ArticleAiSummaryResponse;
import in.koreatech.koin.domain.community.article.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatUserBlockService;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.model.TranslationResource;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.infrastructure.s3.client.S3ClientWrapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TranslationSourceReader {
    private final ArticleRepository articleRepository;
    private final LostItemArticleRepository lostItemArticleRepository;
    private final ArticleAiSummaryService summaryService;
    private final S3ClientWrapper s3Client;
    private final TeamRecruitmentRepository recruitmentRepository;
    private final LostItemChatRoomInfoRepository lostItemRoomRepository;
    private final LostItemChatUserBlockService blockService;
    private final ChatMessageRedisRepository lostItemMessageRepository;
    private final TeamRecruitmentChatRoomRepository teamRoomRepository;
    private final TeamRecruitmentChatMemberRepository teamMemberRepository;
    private final TeamRecruitmentChatMessageRepository teamMessageRepository;
    private final UserRepository userRepository;

    // Solar 호출 전후 각각 새 영속성 컨텍스트에서 원문과 접근 권한을 확인한다.
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public TranslationSource load(TranslationResource resource, Integer userId) {
        return switch (resource.type()) {
            case ARTICLE_SUMMARY -> summary(resource);
            case LOST_ITEM -> lostItem(resource);
            case RECRUITMENT -> recruitment(resource);
            case LOST_ITEM_MESSAGE -> lostItemMessage(resource, userId);
            case RECRUITMENT_MESSAGE -> teamMessage(resource, userId);
        };
    }

    private TranslationSource summary(TranslationResource resource) {
        var article = articleRepository.getById(integerId(resource));
        String content = article.getContent();
        String contentUrl = content == null ? null : content.trim();
        if (s3Client.isCustomDomainUrl(contentUrl)) {
            content = s3Client.getContentFromCustomDomainUrl(contentUrl);
        }
        var response = ArticleAiSummaryResponse.from(summaryService.readReadySummary(article, content));
        var items = response.items().stream()
            .map(item -> Map.<String, Object>of("icon", item.icon(), "text", item.text())).toList();
        Map<String, String> texts = new LinkedHashMap<>();
        for (int i = 0; i < response.items().size(); i++) {
            String text = response.items().get(i).text();
            if (hasLetters(text)) {
                texts.put("item_" + i, text);
            }
        }
        TranslationReason reason = response.status() != ArticleAiSummaryResponse.Status.SUCCESS
            ? TranslationReason.SOURCE_NOT_READY : texts.isEmpty() ? TranslationReason.UNSUPPORTED_CONTENT : null;
        return TranslationSource.of(resource, TranslationSourceFields.summary(items), texts, reason);
    }

    private TranslationSource lostItem(TranslationResource resource) {
        var article = articleRepository.getById(integerId(resource));
        if (!Objects.equals(article.getBoard().getId(), LOST_ITEM_BOARD_ID)) {
            throw ArticleNotFoundException.withDetail("articleId: " + article.getId());
        }
        var item = lostItemArticleRepository.getByArticleId(article.getId());
        return fields(resource, TranslationSourceFields.lostItem(item.getFoundPlace(), article.getContent()), false);
    }

    private TranslationSource recruitment(TranslationResource resource) {
        var recruitment = recruitmentRepository.findById(integerId(resource))
            .filter(value -> !value.isDeleted())
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_NOT_FOUND));
        return fields(resource, TranslationSourceFields.recruitment(recruitment.getTitle(),
            recruitment.getDescription(), recruitment.getQualification()), false);
    }

    private TranslationSource lostItemMessage(TranslationResource resource, Integer userId) {
        authorize(userId, UserType.GENERAL, UserType.STUDENT, UserType.COUNCIL);
        var room = lostItemRoomRepository.getByArticleIdAndChatRoomId(resource.parentId(), resource.roomId());
        if (!Objects.equals(userId, room.getAuthorId()) && !Objects.equals(userId, room.getOwnerId())) {
            throw AuthorizationException.withDetail("채팅방 참여자가 아닙니다.");
        }
        blockService.validateBlockState(resource.parentId(), resource.roomId(), userId);
        var article = articleRepository.getById(resource.parentId());
        if (!Objects.equals(article.getBoard().getId(), LOST_ITEM_BOARD_ID)) {
            throw ArticleNotFoundException.withDetail("articleId: " + resource.parentId());
        }
        lostItemArticleRepository.getByArticleId(resource.parentId());
        Integer partnerId = Objects.equals(userId, room.getAuthorId()) ? room.getOwnerId() : room.getAuthorId();
        if (partnerId == null || userRepository.findById(partnerId).filter(user -> !user.isDeleted()).isEmpty()) {
            throw CustomException.of(NOT_FOUND_CHAT_PARTNER);
        }
        Long messageId;
        try {
            messageId = Long.valueOf(resource.resourceId());
        } catch (NumberFormatException exception) {
            throw CustomException.of(TRANSLATION_SOURCE_NOT_FOUND);
        }
        var message = lostItemMessageRepository.findByArticleIdAndChatRoomIdAndId(
                resource.parentId(), resource.roomId(), messageId)
            .filter(value -> !Boolean.TRUE.equals(value.getIsDeleted()))
            .orElseThrow(() -> CustomException.of(TRANSLATION_SOURCE_NOT_FOUND));
        return fields(resource, TranslationSourceFields.message(message.getContents()),
            Boolean.TRUE.equals(message.getIsImage()));
    }

    private TranslationSource teamMessage(TranslationResource resource, Integer userId) {
        authorize(userId, UserType.STUDENT);
        var room = teamRoomRepository.findById(resource.roomId())
            .orElseThrow(() -> CustomException.of(TRANSLATION_SOURCE_NOT_FOUND));
        if (!Objects.equals(room.getRecruitment().getId(), resource.parentId())) {
            throw CustomException.of(TRANSLATION_SOURCE_NOT_FOUND);
        }
        if (!teamMemberRepository.existsByChatRoom_IdAndUser_Id(resource.roomId(), userId)) {
            throw CustomException.of(TEAM_RECRUITMENT_CHAT_FORBIDDEN);
        }
        var message = teamMessageRepository.findByIdAndChatRoom_Id(integerId(resource), resource.roomId())
            .orElseThrow(() -> CustomException.of(TRANSLATION_SOURCE_NOT_FOUND));
        return fields(resource, TranslationSourceFields.message(message.getContent()),
            Boolean.TRUE.equals(message.getIsImage()));
    }

    private void authorize(Integer userId, UserType... permitted) {
        if (userId == null) {
            throw AuthenticationException.withDetail("로그인이 필요합니다.");
        }
        userRepository.getById(userId).authorizeAndGetId(permitted);
    }

    private int integerId(TranslationResource resource) {
        try {
            return Integer.parseInt(resource.resourceId());
        } catch (NumberFormatException exception) {
            throw CustomException.of(ILLEGAL_ARGUMENT);
        }
    }

    private TranslationSource fields(TranslationResource resource, Map<String, Object> data, boolean image) {
        Map<String, String> texts = new LinkedHashMap<>();
        if (!image) {
            data.forEach((key, value) -> {
                if (value instanceof String text && hasLetters(text)) {
                    texts.put(key, text);
                }
            });
        }
        return TranslationSource.of(resource, data, texts,
            texts.isEmpty() ? TranslationReason.UNSUPPORTED_CONTENT : null);
    }

    private boolean hasLetters(String text) {
        return text != null && text.codePoints().anyMatch(Character::isLetter);
    }
}
