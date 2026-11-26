package in.koreatech.koin.unit.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.repository.NotificationJdbcRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.fcm.FcmSendRequest;
import in.koreatech.koin.infrastructure.fcm.FcmSendResponse;

class NotificationServiceTest {

    @Test
    void 일반_알림_배치는_기존_payload와_개별_발송_결과를_유지한다() {
        FcmClient fcm = mock(FcmClient.class);
        NotificationJdbcRepository history = mock(NotificationJdbcRepository.class);
        NotificationService service = new NotificationService(fcm, mock(UserRepository.class), history,
            mock(NotificationSubscribeRepository.class));
        User user = User.builder().id(7).userType(UserType.STUDENT).isAuthed(true).isDeleted(false)
            .deviceToken("test-device").build();
        Notification first = Notification.of(MobileAppPath.DINING, "dining?id=1", "첫 제목", "첫 내용", null, user);
        Notification second = Notification.of(MobileAppPath.SHOP, "shop?id=2", "다음 제목", "다음 내용", null, user);
        when(fcm.sendMessages(anyList())).thenReturn(List.of(
            FcmSendResponse.succeeded(), FcmSendResponse.failed("UNREGISTERED", "UNREGISTERED")));

        service.pushNotifications(List.of(first, second));

        ArgumentCaptor<List<FcmSendRequest>> requests = ArgumentCaptor.forClass(List.class);
        verify(fcm).sendMessages(requests.capture());
        assertThat(requests.getValue()).extracting(FcmSendRequest::schemeUri).containsExactly("dining?id=1", "shop?id=2");
        assertThat(requests.getValue()).allSatisfy(request -> assertThat(request.roomId()).isNull());
        assertThat(first.getPushSuccess()).isTrue();
        assertThat(second.getPushSuccess()).isFalse();
        assertThat(second.getFcmErrorCode()).isEqualTo("UNREGISTERED");
        verify(history).batchInsert(List.of(first, second));
    }
}
