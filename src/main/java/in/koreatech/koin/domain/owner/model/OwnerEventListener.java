package in.koreatech.koin.domain.owner.model;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRedisRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.global.domain.slack.SlackClient;
import in.koreatech.koin.global.domain.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class OwnerEventListener {

    private final SlackClient slackClient;
    private final ShopRepository shopRepository;
    private final SlackNotificationFactory slackNotificationFactory;
    private final OwnerInVerificationRedisRepository ownerInVerificationRedisRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerEmailRequest(OwnerEmailRequestEvent event) {
        var notification = slackNotificationFactory.generateOwnerEmailVerificationRequestNotification(event.email());
        slackClient.sendMessage(notification);
    }

    /**
     * 사장님 회원가입 시 상점 id Redis 임시저장
     * <p>
     * 추후 어드민에서 승인 시 상점 id를 기준으로 업데이트 수행
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerRegister(OwnerRegisterEvent event) {
        Owner owner = event.owner();
        ownerInVerificationRedisRepository.deleteByVerify(owner.getUser().getEmail());
        String shopsName = shopRepository.findAllByOwnerId(owner.getId())
            .stream().map(Shop::getName).collect(Collectors.joining(", "));
        var notification = slackNotificationFactory.generateOwnerRegisterRequestNotification(
            owner.getUser().getName(),
            shopsName
        );
        slackClient.sendMessage(notification);
    }
}
