package in.koreatech.koin.infrastructure.s3.eventlistener;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.ImageDeletedEvent;
import in.koreatech.koin.common.event.ImageSensitiveDeletedEvent;
import in.koreatech.koin.common.event.ImagesDeletedEvent;
import in.koreatech.koin.common.event.ImagesSensitiveDeletedEvent;
import in.koreatech.koin.infrastructure.s3.client.CloudFrontClientWrapper;
import in.koreatech.koin.infrastructure.s3.client.S3Client;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class S3EventListener {

    private final S3Client s3Client;
    private final CloudFrontClientWrapper cloudFrontClientWrapper;

    @Async
    @TransactionalEventListener
    public void onImageDeleted(ImageDeletedEvent event) {
        String s3Key = s3Client.extractKeyFromUrl(event.imageUrl());
        s3Client.deleteFile(s3Key);
    }

    @Async
    @TransactionalEventListener
    public void onImagesDeleted(ImagesDeletedEvent event) {
        List<String> s3Keys = s3Client.extractKeysFromUrls(event.imageUrls());
        s3Client.deleteFiles(s3Keys);
    }

    @Async
    @TransactionalEventListener
    public void onSensitiveImageDeleted(ImageSensitiveDeletedEvent event) {
        String s3Key = s3Client.extractKeyFromUrl(event.imageUrl());
        s3Client.deleteFile(s3Key);
        cloudFrontClientWrapper.invalidate(s3Key);
    }

    @Async
    @TransactionalEventListener
    public void onSensitiveImagesDeleted(ImagesSensitiveDeletedEvent event) {
        List<String> s3Keys = s3Client.extractKeysFromUrls(event.imageUrls());
        s3Client.deleteFiles(s3Keys);
        cloudFrontClientWrapper.invalidateAll(s3Keys);
    }
}
