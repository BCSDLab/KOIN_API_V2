package in.koreatech.koin.unit.admin.ownershop;

import static in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceRequestProcessRequest.ProcessType.APPROVE;
import static in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceRequestProcessRequest.ProcessType.REJECT;
import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus.APPROVED;
import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus.PENDING;
import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus.REJECTED;
import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_ORDERABLE_SHOP;
import static in.koreatech.koin.global.code.ApiResponseCode.ORDER_SERVICE_REQUEST_ALREADY_PROCESSED;
import static in.koreatech.koin.global.code.ApiResponseCode.ORDER_SERVICE_REQUEST_INCOMPLETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceRequestProcessRequest;
import in.koreatech.koin.admin.ownershop.repository.AdminShopOrderServiceRequestRepository;
import in.koreatech.koin.admin.ownershop.service.AdminShopOrderServiceRequestService;
import in.koreatech.koin.domain.order.shop.service.OrderableShopPromotionService;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceMenuSnapshot;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class AdminShopOrderServiceRequestServiceTest {

    private static final Integer REQUEST_ID = 1;
    private static final LocalDateTime APPROVED_AT = LocalDateTime.of(2026, 11, 9, 12, 0);

    @InjectMocks
    private AdminShopOrderServiceRequestService adminShopOrderServiceRequestService;

    @Mock
    private AdminShopOrderServiceRequestRepository adminShopOrderServiceRequestRepository;

    @Mock
    private OrderableShopPromotionService orderableShopPromotionService;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-11-09T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Nested
    @DisplayName("주문 서비스 신청 수락")
    class ApproveOrderServiceRequestTest {

        private final AdminShopOrderServiceRequestProcessRequest request =
            new AdminShopOrderServiceRequestProcessRequest(APPROVE, null);

        @Test
        @DisplayName("신청을 수락하면 상점을 승격하고 승인 일시를 기록한다")
        void approveOrderServiceRequest() {
            ShopOrderServiceRequest application = createApplication("홍길동", createMenus());
            when(adminShopOrderServiceRequestRepository.getByIdForUpdate(REQUEST_ID)).thenReturn(application);

            adminShopOrderServiceRequestService.processOrderServiceRequest(REQUEST_ID, request);

            verify(orderableShopPromotionService).promote(application);
            assertThat(application.getRequestStatus()).isEqualTo(APPROVED);
            assertThat(application.getApprovedAt()).isEqualTo(APPROVED_AT);
        }

        @Test
        @DisplayName("상점 승격에 실패하면 신청은 대기 상태를 유지한다")
        void keepPendingWhenPromotionFails() {
            ShopOrderServiceRequest application = createApplication("홍길동", createMenus());
            when(adminShopOrderServiceRequestRepository.getByIdForUpdate(REQUEST_ID)).thenReturn(application);
            doThrow(CustomException.of(DUPLICATE_ORDERABLE_SHOP))
                .when(orderableShopPromotionService).promote(application);

            assertThatThrownBy(() -> adminShopOrderServiceRequestService.processOrderServiceRequest(REQUEST_ID, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DUPLICATE_ORDERABLE_SHOP);

            assertThat(application.getRequestStatus()).isEqualTo(PENDING);
            assertThat(application.getApprovedAt()).isNull();
        }

        @Test
        @DisplayName("이관 메뉴가 없는 기존 신청은 수락할 수 없다")
        void cannotApproveWithoutMenus() {
            ShopOrderServiceRequest application = createApplication("홍길동", null);
            when(adminShopOrderServiceRequestRepository.getByIdForUpdate(REQUEST_ID)).thenReturn(application);

            assertThatThrownBy(() -> adminShopOrderServiceRequestService.processOrderServiceRequest(REQUEST_ID, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ORDER_SERVICE_REQUEST_INCOMPLETE);

            verifyNoInteractions(orderableShopPromotionService);
            assertThat(application.getRequestStatus()).isEqualTo(PENDING);
        }

        @Test
        @DisplayName("예금주가 없는 기존 신청은 수락할 수 없다")
        void cannotApproveWithoutAccountHolder() {
            ShopOrderServiceRequest application = createApplication(null, createMenus());
            when(adminShopOrderServiceRequestRepository.getByIdForUpdate(REQUEST_ID)).thenReturn(application);

            assertThatThrownBy(() -> adminShopOrderServiceRequestService.processOrderServiceRequest(REQUEST_ID, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ORDER_SERVICE_REQUEST_INCOMPLETE);

            verifyNoInteractions(orderableShopPromotionService);
            assertThat(application.getRequestStatus()).isEqualTo(PENDING);
        }

        @Test
        @DisplayName("반려된 신청을 다시 수락할 수 없다")
        void cannotApproveRejectedRequest() {
            ShopOrderServiceRequest application = createApplication("홍길동", createMenus());
            application.reject("서류 보완이 필요합니다.");
            when(adminShopOrderServiceRequestRepository.getByIdForUpdate(REQUEST_ID)).thenReturn(application);

            assertThatThrownBy(() -> adminShopOrderServiceRequestService.processOrderServiceRequest(REQUEST_ID, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ORDER_SERVICE_REQUEST_ALREADY_PROCESSED);

            verifyNoInteractions(orderableShopPromotionService);
        }
    }

    @Nested
    @DisplayName("주문 서비스 신청 반려")
    class RejectOrderServiceRequestTest {

        private final AdminShopOrderServiceRequestProcessRequest request =
            new AdminShopOrderServiceRequestProcessRequest(REJECT, "서류 보완이 필요합니다.");

        @Test
        @DisplayName("기존 신청을 반려하면 상점 승격 없이 반려 사유를 기록한다")
        void rejectLegacyOrderServiceRequest() {
            ShopOrderServiceRequest application = createApplication(null, null);
            when(adminShopOrderServiceRequestRepository.getByIdForUpdate(REQUEST_ID)).thenReturn(application);

            adminShopOrderServiceRequestService.processOrderServiceRequest(REQUEST_ID, request);

            assertThat(application.getRequestStatus()).isEqualTo(REJECTED);
            assertThat(application.getRejectionReason()).isEqualTo("서류 보완이 필요합니다.");
            assertThat(application.getApprovedAt()).isNull();
            verifyNoInteractions(orderableShopPromotionService);
        }

        @Test
        @DisplayName("승인된 신청을 반려할 수 없다")
        void cannotRejectApprovedRequest() {
            ShopOrderServiceRequest application = createApplication("홍길동", createMenus());
            application.approve(APPROVED_AT);
            when(adminShopOrderServiceRequestRepository.getByIdForUpdate(REQUEST_ID)).thenReturn(application);

            assertThatThrownBy(() -> adminShopOrderServiceRequestService.processOrderServiceRequest(REQUEST_ID, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ORDER_SERVICE_REQUEST_ALREADY_PROCESSED);

            assertThat(application.getRequestStatus()).isEqualTo(APPROVED);
            assertThat(application.getRejectionReason()).isNull();
        }
    }

    private ShopOrderServiceRequest createApplication(String accountHolder, List<ShopOrderServiceMenuSnapshot> menus) {
        return ShopOrderServiceRequest.builder()
            .accountHolder(accountHolder)
            .menus(menus)
            .build();
    }

    private List<ShopOrderServiceMenuSnapshot> createMenus() {
        return List.of(new ShopOrderServiceMenuSnapshot(
            101, "탕수육", null, List.of(),
            List.of(new ShopOrderServiceMenuSnapshot.Price(null, 18000)), List.of(), List.of()
        ));
    }
}
