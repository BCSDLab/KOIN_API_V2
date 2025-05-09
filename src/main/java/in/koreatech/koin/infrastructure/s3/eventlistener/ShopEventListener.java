package in.koreatech.koin.infrastructure.s3.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.*;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.ShopImageDeletedEvent;
import in.koreatech.koin._common.event.ShopImagesDeletedEvent;
import in.koreatech.koin.infrastructure.s3.client.S3Client;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShopEventListener {

    private final S3Client s3Client;

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onShopImageDeleted(ShopImageDeletedEvent event) {
        s3Client.deleteFile(event.s3Key());
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onShopImagesDeleted(ShopImagesDeletedEvent event) {
        s3Client.deleteFiles(event.s3Keys());
    }
}
