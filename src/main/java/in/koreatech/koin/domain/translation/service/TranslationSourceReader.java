package in.koreatech.koin.domain.translation.service;

import static in.koreatech.koin.domain.community.article.service.ArticleService.LOST_ITEM_BOARD_ID;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
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
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuRepository;
import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenuResponse;
import in.koreatech.koin.domain.shop.repository.menu.MenuRepository;
import in.koreatech.koin.domain.shop.repository.menu.MenuCategoryRepository;

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
    private final DiningRepository diningRepository;
    private final ShopRepository shopRepository;
    private final OrderableShopRepository orderableShopRepository;
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final OrderableShopMenuRepository orderableShopMenuRepository;
    private final CallvanParticipantRepository callvanParticipantRepository;
    private final CallvanChatMessageRepository callvanMessageRepository;

    // Solar 호출 전후 각각 새 영속성 컨텍스트에서 원문과 접근 권한을 확인한다.
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public TranslationSource load(TranslationResource resource, Integer userId) {
        return switch (resource.type()) {
            case DINING -> dining(resource);
            case SHOP -> fields(resource, TranslationSourceFields.shop(shopRepository.getById(integerId(resource)).getDescription()), false);
            case SHOP_MENU_CATEGORY -> shopMenuCategory(resource);
            case ORDERABLE_SHOP_MENU_GROUP -> orderableShopMenuGroup(resource);
            case ORDERABLE_SHOP_MENU -> orderableShopMenu(resource);
            case SHOP_INFO -> shopInfo(resource);
            case SHOP_MENU -> shopMenu(resource);
            case ARTICLE_SUMMARY -> summary(resource);
            case LOST_ITEM -> lostItem(resource);
            case RECRUITMENT -> recruitment(resource);
            case LOST_ITEM_MESSAGE -> lostItemMessage(resource, userId);
            case RECRUITMENT_MESSAGE -> teamMessage(resource, userId);
            case CALLVAN_MESSAGE -> callvanMessage(resource, userId);
        };
    }

    private TranslationSource dining(TranslationResource resource) {
        var dining = diningRepository.findById(integerId(resource))
            .orElseThrow(() -> CustomException.of(TRANSLATION_SOURCE_NOT_FOUND));
        var menu = dining.getMenu();
        Map<String, String> texts = new LinkedHashMap<>();
        if (menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                String text = menu.get(i);
                if (hasLetters(text)) {
                    texts.put("menu_" + i, text);
                }
            }
        }
        return TranslationSource.of(resource, TranslationSourceFields.dining(menu), texts,
            texts.isEmpty() ? TranslationReason.UNSUPPORTED_CONTENT : null);
    }

    private TranslationSource shopInfo(TranslationResource resource) {
        int shopId = integerId(resource);
        if (!orderableShopRepository.existsByShop_Id(shopId)) {
            throw CustomException.of(TRANSLATION_SOURCE_NOT_FOUND);
        }
        var shop = shopRepository.getById(shopId);
        return fields(resource, TranslationSourceFields.shopInfo(shop.getIntroduction(), shop.getNotice(), shop.getAddressDetail()), false);
    }

    private TranslationSource shopMenuCategory(TranslationResource resource) {
        shopRepository.getById(resource.parentId());
        var category = menuCategoryRepository.getById(integerId(resource));
        if (!Objects.equals(category.getShop().getId(), resource.parentId())) {
            throw CustomException.of(TRANSLATION_SOURCE_NOT_FOUND);
        }
        return fields(resource, TranslationSourceFields.name(category.getName()), false);
    }

    private TranslationSource orderableShopMenuGroup(TranslationResource resource) {
        var orderableShop = orderableShopRepository.getByIdWithMenus(resource.parentId());
        shopRepository.getById(orderableShop.getShop().getId());
        var group = orderableShop.getMenuGroups().stream()
            .filter(value -> Objects.equals(value.getId(), integerId(resource)))
            .filter(value -> !Boolean.TRUE.equals(value.getIsDeleted()))
            .findFirst().orElseThrow(() -> CustomException.of(TRANSLATION_SOURCE_NOT_FOUND));
        return fields(resource, TranslationSourceFields.name(group.getName()), false);
    }

    private TranslationSource orderableShopMenu(TranslationResource resource) {
        var orderableShop = orderableShopRepository.getById(resource.parentId());
        shopRepository.getById(orderableShop.getShop().getId());
        var menu = orderableShopMenuRepository.getByIdWithMenuOptionGroups(integerId(resource));
        if (Boolean.TRUE.equals(menu.getIsDeleted())
            || !Objects.equals(menu.getOrderableShop().getId(), resource.parentId())) {
            throw CustomException.of(TRANSLATION_SOURCE_NOT_FOUND);
        }
        var response = OrderableShopMenuResponse.from(menu);
        Map<String, String> texts = new LinkedHashMap<>();
        addText(texts, "name", response.name());
        addText(texts, "description", response.description());
        for (int i = 0; i < response.prices().size(); i++) {
            addText(texts, "price_" + i + "_name", response.prices().get(i).name());
        }
        for (int i = 0; i < response.optionGroups().size(); i++) {
            var group = response.optionGroups().get(i);
            String key = "group_" + i;
            addText(texts, key + "_name", group.name());
            addText(texts, key + "_description", group.description());
            for (int j = 0; j < group.options().size(); j++) {
                addText(texts, key + "_option_" + j + "_name", group.options().get(j).name());
            }
        }
        return TranslationSource.of(resource, TranslationSourceFields.orderableShopMenu(response), texts,
            texts.isEmpty() ? TranslationReason.UNSUPPORTED_CONTENT : null);
    }

    private void addText(Map<String, String> texts, String key, String value) {
        if (hasLetters(value)) {
            texts.put(key, value);
        }
    }

    private TranslationSource shopMenu(TranslationResource resource) {
        shopRepository.getById(resource.parentId());
        var menu = menuRepository.getById(integerId(resource));
        if (!Objects.equals(menu.getShop().getId(), resource.parentId()) || menu.isHidden()) {
            throw CustomException.of(TRANSLATION_SOURCE_NOT_FOUND);
        }
        var optionPrices = menu.hasMultipleOption() ? menu.getMenuOptions().stream()
            .map(option -> TranslationSourceFields.optionPrice(option.getOption(), option.getPrice())).toList() : null;
        Map<String, String> texts = new LinkedHashMap<>();
        addText(texts, "name", menu.getName());
        addText(texts, "description", menu.getDescription());
        if (optionPrices != null) {
            for (int i = 0; i < optionPrices.size(); i++) {
                addText(texts, "option_" + i, (String)optionPrices.get(i).get("option"));
            }
        }
        return TranslationSource.of(resource,
            TranslationSourceFields.shopMenu(menu.getName(), menu.getDescription(), optionPrices), texts,
            texts.isEmpty() ? TranslationReason.UNSUPPORTED_CONTENT : null);
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

    private TranslationSource callvanMessage(TranslationResource resource, Integer userId) {
        authorize(userId, UserType.STUDENT);
        if (!callvanParticipantRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(resource.parentId(), userId)) {
            throw CustomException.of(FORBIDDEN_PARTICIPANT);
        }
        var message = callvanMessageRepository.findInActivePost(integerId(resource), resource.parentId())
            .orElseThrow(() -> CustomException.of(TRANSLATION_SOURCE_NOT_FOUND));
        return fields(resource, TranslationSourceFields.message(message.getContent()),
            Boolean.TRUE.equals(message.getIsImage()) || message.getMessageType() == CallvanMessageType.IMAGE);
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
