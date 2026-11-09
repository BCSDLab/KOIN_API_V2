package in.koreatech.koin.domain.translation.controller;

import static in.koreatech.koin.domain.translation.model.TranslationType.*;
import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.koreatech.koin.domain.translation.dto.*;
import in.koreatech.koin.domain.translation.model.TranslationResource;
import in.koreatech.koin.domain.translation.service.TranslationService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TranslationController implements TranslationApi {
    private final TranslationService service;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.Name>> shopMenuCategory(Integer shopId, Integer id,
        @RequestBody @Valid TranslationRequest request) {
        return translated(new TranslationResource(SHOP_MENU_CATEGORY, id.toString(), shopId, 0),
            request, null, TranslationData.Name.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.Name>> orderableShopMenuGroup(Integer orderableShopId, Integer id,
        @RequestBody @Valid TranslationRequest request) {
        return translated(new TranslationResource(ORDERABLE_SHOP_MENU_GROUP, id.toString(), orderableShopId, 0),
            request, null, TranslationData.Name.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.OrderableShopMenu>> orderableShopMenu(
        Integer orderableShopId, Integer orderableShopMenuId, @RequestBody @Valid TranslationRequest request) {
        return translated(new TranslationResource(ORDERABLE_SHOP_MENU, orderableShopMenuId.toString(), orderableShopId, 0),
            request, null, TranslationData.OrderableShopMenu.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.ShopInfo>> shopInfo(Integer id, @RequestBody @Valid TranslationRequest request) {
        return translated(TranslationResource.article(SHOP_INFO, id), request, null, TranslationData.ShopInfo.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.Dining>> dining(Integer id, @RequestBody @Valid TranslationRequest request) {
        return translated(TranslationResource.article(DINING, id), request, null, TranslationData.Dining.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.Shop>> shop(Integer id, @RequestBody @Valid TranslationRequest request) {
        return translated(TranslationResource.article(SHOP, id), request, null, TranslationData.Shop.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.ShopMenu>> shopMenu(Integer shopId, Integer id,
        @RequestBody @Valid TranslationRequest request) {
        return translated(new TranslationResource(SHOP_MENU, id.toString(), shopId, 0), request, null, TranslationData.ShopMenu.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.Summary>> summary(Integer id, @RequestBody @Valid TranslationRequest request) {
        return translated(TranslationResource.article(ARTICLE_SUMMARY, id), request, null, TranslationData.Summary.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.LostItem>> lostItem(Integer id, @RequestBody @Valid TranslationRequest request) {
        return translated(TranslationResource.article(LOST_ITEM, id), request, null, TranslationData.LostItem.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.Recruitment>> recruitment(Integer id, @RequestBody @Valid TranslationRequest request) {
        return translated(TranslationResource.article(RECRUITMENT, id), request, null, TranslationData.Recruitment.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.Message>> lostItemMessage(Integer articleId, Integer chatRoomId,
        Long messageId, @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId, @RequestBody @Valid TranslationRequest request) {
        return translated(TranslationResource.message(LOST_ITEM_MESSAGE, articleId, chatRoomId, messageId.toString()),
            request, userId, TranslationData.Message.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<TranslationData.Message>> recruitmentMessage(Integer recruitmentId, Integer chatRoomId,
        Integer messageId, @Auth(permit = {STUDENT}) Integer userId, @RequestBody @Valid TranslationRequest request) {
        return translated(TranslationResource.message(RECRUITMENT_MESSAGE, recruitmentId, chatRoomId, messageId.toString()),
            request, userId, TranslationData.Message.class);
    }

    @Override
    public ResponseEntity<TranslationResponse<Map<String, Object>>> result(String translationId, @UserId Integer userId) {
        var response = service.get(translationId, userId);
        return headers(response, false).body(response);
    }

    private <T> ResponseEntity<TranslationResponse<T>> translated(TranslationResource resource, TranslationRequest request,
        Integer userId, Class<T> dataType) {
        var response = service.translate(resource, request, userId);
        T data = response.data() == null ? null : objectMapper.convertValue(response.data(), dataType);
        var typed = new TranslationResponse<>(response.translationId(), response.status(), response.targetLanguage(),
            response.sourceVersion(), response.reasonCode(), response.retryable(), data);
        return headers(response, true).body(typed);
    }

    private ResponseEntity.BodyBuilder headers(TranslationResponse<?> response, boolean post) {
        var builder = ResponseEntity.status(post && "PENDING".equals(response.status()) ? 202 : 200)
            .cacheControl(CacheControl.noStore());
        long retryAfter = service.retryAfterSeconds(response);
        if (retryAfter > 0) {
            builder.header(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter));
        }
        return builder;
    }
}
