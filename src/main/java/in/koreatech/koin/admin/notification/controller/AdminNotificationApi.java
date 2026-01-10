package in.koreatech.koin.admin.notification.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_DETAIL_SUBSCRIBE_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.admin.notification.dto.AdminNotificationRequest;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Notification: 알림", description = "알림 관련 API")
@RequestMapping("/admin/notification")
public interface AdminNotificationApi {

    @ApiResponseCodes(value = {
        OK,
        INVALID_DETAIL_SUBSCRIBE_TYPE
    })
    @Operation(summary = "푸시 알림 전송", description = """
        ## 구독 타입
        - SHOP_EVENT: 상점 이벤트
        - REVIEW_PROMPT: 리뷰 작성 유도
        - DINING_SOLD_OUT: 식당 품절 (세부 타입: BREAKFAST, LUNCH, DINNER)
        - DINING_IMAGE_UPLOAD: 식단 이미지 업로드
        - ARTICLE_KEYWORD: 게시글 키워드
        - LOST_ITEM_CHAT: 분실물 채팅
        - MARKETING: 마케팅

        ## 세부 구독 타입
        - BREAKFAST: 아침 (DINING_SOLD_OUT에 해당)
        - LUNCH: 점심 (DINING_SOLD_OUT에 해당)
        - DINNER: 저녁 (DINING_SOLD_OUT에 해당)

        ## 에러
        - INVALID_DETAIL_SUBSCRIBE_TYPE (400): 세부 구독 타입이 구독 타입에 속하지 않습니다.
        """)
    @PostMapping("/send")
    ResponseEntity<Void> sendNotification(
        @Valid @RequestBody AdminNotificationRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
