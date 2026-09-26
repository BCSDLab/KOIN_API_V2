package in.koreatech.koin.unit.domain.notification.model;

import static in.koreatech.koin.common.model.MobileAppPath.ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixture.UserFixture;

class NotificationFactoryTest {

    private final NotificationFactory notificationFactory = new NotificationFactory();
    private final User user = UserFixture.id_설정_코인_유저(1);
    private final LocalDateTime estimatedAt = LocalDateTime.of(2026, 9, 26, 18, 30);

    @Test
    void 배달_주문이_접수되면_상태를_제목으로_알리고_상점명과_예상_도착을_본문에_담는다() {
        Notification notification = notificationFactory.generateOrderNotification(
            ORDER, 42, "김밥천국", "COOKING", true, estimatedAt, user
        );

        assertThat(notification.getTitle()).isEqualTo("주문이 접수되었어요");
        assertThat(notification.getMessage()).isEqualTo("김밥천국에서 조리를 시작했어요.\n예상 도착: 9월 26일 18:30");
        assertThat(notification.getSchemeUri()).isEqualTo("order?id=42");
    }

    @Test
    void 포장_주문이_접수되면_포장_완료_예정_시간을_본문에_담는다() {
        Notification notification = notificationFactory.generateOrderNotification(
            ORDER, 42, "김밥천국", "COOKING", false, estimatedAt, user
        );

        assertThat(notification.getTitle()).isEqualTo("주문이 접수되었어요");
        assertThat(notification.getMessage()).isEqualTo("김밥천국에서 조리를 시작했어요.\n포장 완료 예정: 9월 26일 18:30");
    }

    @Test
    void 배달이_시작되면_예상_도착_시간을_본문에_담는다() {
        Notification notification = notificationFactory.generateOrderNotification(
            ORDER, 42, "김밥천국", "DELIVERING", true, estimatedAt, user
        );

        assertThat(notification.getTitle()).isEqualTo("배달이 시작됐어요");
        assertThat(notification.getMessage()).isEqualTo("김밥천국에서 주문하신 음식이 배달 중이에요.\n예상 도착: 9월 26일 18:30");
    }

    @ParameterizedTest
    @CsvSource({
        "PACKAGED, 포장이 완료됐어요, 김밥천국에서 주문을 수령해 주세요.",
        "PICKED_UP, 주문 수령이 완료됐어요, '김밥천국 주문, 맛있게 드세요!'",
        "DELIVERED, 배달이 완료됐어요, '김밥천국 주문, 맛있게 드세요!'",
        "CANCELED, 주문이 취소됐어요, 김밥천국 주문의 자세한 내용은 주문 내역을 확인해 주세요."
    })
    void 주문_상태별_제목과_본문을_생성한다(String status, String title, String message) {
        Notification notification = notificationFactory.generateOrderNotification(
            ORDER, 42, "김밥천국", status, true, estimatedAt, user
        );

        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getMessage()).isEqualTo(message);
    }

    @Test
    void 알림_대상이_아닌_상태는_거부한다() {
        assertThatThrownBy(() -> notificationFactory.generateOrderNotification(
            ORDER, 42, "김밥천국", "CONFIRMING", true, estimatedAt, user
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
