package in.koreatech.koin.infrastructure.s3.eventlistener;

import static org.springframework.transaction.event.TransactionPhase.*;

import java.util.List;

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
        String s3Key = s3Client.extractKeyFromUrl(event.imageUrl());
        s3Client.deleteFile(s3Key);
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onShopImagesDeleted(ShopImagesDeletedEvent event) {
        List<String> s3Keys = s3Client.extractKeysFromUrls(event.imageUrls());
        s3Client.deleteFiles(s3Keys);
    }
}
