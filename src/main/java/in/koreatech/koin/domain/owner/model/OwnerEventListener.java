package in.koreatech.koin.domain.owner.model;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.owner.model.dto.OwnerEmailRequestEvent;
import in.koreatech.koin.domain.owner.model.dto.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.model.dto.OwnerSmsRequestEvent;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.global.domain.slack.SlackClient;
import in.koreatech.koin.global.domain.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class OwnerEventListener {

    private final SlackClient slackClient;
    private final OwnerShopRedisRepository ownerShopRedisRepository;
    private final SlackNotificationFactory slackNotificationFactory;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerEmailRequest(OwnerEmailRequestEvent event) {
        var notification = slackNotificationFactory.generateOwnerEmailVerificationRequestNotification(event.email());
        slackClient.sendMessage(notification);
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerPhoneRequest(OwnerSmsRequestEvent ownerPhoneRequestEvent) {
        var notification = slackNotificationFactory.generateOwnerPhoneVerificationRequestNotification(
            ownerPhoneRequestEvent.phoneNumber());
        slackClient.sendMessage(notification);
    }

    /**
     * 사장님 회원가입 시 상점 id Redis 임시저장
     * <p>
     * 추후 어드민에서 승인 시 상점 id를 기준으로 업데이트 수행
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerRegisterBySms(OwnerRegisterEvent event) {
        var notification = slackNotificationFactory.generateOwnerRegisterRequestNotification(
            event.ownerName(),
            shopsName(event.ownerId())
        );
        slackClient.sendMessage(notification);
    }

    private String shopsName(Integer id) {
        return ownerShopRedisRepository.findById(id).getShopName();
    }
}
