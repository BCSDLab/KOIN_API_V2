package in.koreatech.koin.infrastructure.naver.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.*;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.OwnerSmsVerificationSendEvent;
import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin.infrastructure.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverSmsEventListener {

    private final NaverSmsService naverSmsService;

    @TransactionalEventListener
    public void onUserSmsVerificationSendEvent(UserSmsVerificationSendEvent event) {
        naverSmsService.sendVerificationCode(event.verificationCode(), event.phoneNumber());
    }

    @TransactionalEventListener
    public void onOwnerSmsVerificationSendEvent(OwnerSmsVerificationSendEvent event) {
        naverSmsService.sendVerificationCode(event.verificationCode(), event.phoneNumber());
    }
}
