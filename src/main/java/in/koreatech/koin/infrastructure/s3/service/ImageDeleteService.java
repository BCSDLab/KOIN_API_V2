package in.koreatech.koin.infrastructure.s3.service;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import in.koreatech.koin._common.event.ImageDeletedEvent;
import in.koreatech.koin._common.event.ImageSensitiveDeletedEvent;
import in.koreatech.koin._common.event.ImagesDeletedEvent;
import in.koreatech.koin._common.event.ImagesSensitiveDeletedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageDeleteService {

    private final ApplicationEventPublisher eventPublisher;

    public <T> void publishImagesDeletedEvent(Collection<T> images, Function<T, String> extractor) {
        List<String> imageUrls = extractUrls(images, extractor);
        eventPublisher.publishEvent(new ImagesDeletedEvent(imageUrls));
    }

    public <T> void publishImageDeletedEvent(T image, Function<T, String> extractor) {
        String imageUrl = extractor.apply(image);
        eventPublisher.publishEvent(new ImageDeletedEvent(imageUrl));
    }

    public <T> void publishSensitiveImagesDeletedEvent(Collection<T> images, Function<T, String> extractor) {
        List<String> imageUrls = extractUrls(images, extractor);
        eventPublisher.publishEvent(new ImagesSensitiveDeletedEvent(imageUrls));
    }

    public <T> void publishSensitiveImageDeletedEvent(T image, Function<T, String> extractor) {
        String imageUrl = extractor.apply(image);
        eventPublisher.publishEvent(new ImageSensitiveDeletedEvent(imageUrl));
    }

    private <T> List<String> extractUrls(Collection<T> images, Function<T, String> extractor) {
        return images.stream()
            .map(extractor)
            .toList();
    }
}
