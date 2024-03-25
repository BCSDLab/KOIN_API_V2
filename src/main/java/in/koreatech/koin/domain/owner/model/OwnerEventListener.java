package in.koreatech.koin.domain.owner.model;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.global.domain.slack.SlackClient;
import in.koreatech.koin.global.domain.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class OwnerEventListener {

    private final SlackClient slackClient;
    private final ShopRepository shopRepository;
    private final SlackNotificationFactory slackNotificationFactory;
    private final OwnerInVerificationRepository ownerInVerificationRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerEmailRequest(OwnerEmailRequestEvent event) {
        var notification = slackNotificationFactory.generateOwnerEmailVerificationRequestNotification(event.email());
        slackClient.sendMessage(notification);
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOwnerRegister(OwnerRegisterEvent event) {
        Owner owner = event.owner();
        ownerInVerificationRepository.deleteById(owner.getUser().getEmail());
        String shopsName = shopRepository.findAllByOwnerId(owner.getId())
            .stream().map(Shop::getName).collect(Collectors.joining(", "));
        var notification = slackNotificationFactory.generateOwnerRegisterRequestNotification(
            owner.getUser().getName(),
            shopsName
        );
        slackClient.sendMessage(notification);
    }
}
