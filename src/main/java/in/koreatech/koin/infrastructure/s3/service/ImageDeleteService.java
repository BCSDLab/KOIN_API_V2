package in.koreatech.koin.infrastructure.s3.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public <T> void publishImagesModifyEvent(
        Collection<T> oldImages,
        List<String> newImages,
        Function<T, String> extractor
    ) {
        List<String> oldImageUrls = extractUrls(oldImages, extractor);
        List<String> toDeleteUrls = extractDeleteUrls(newImages, oldImageUrls);
        eventPublisher.publishEvent(new ImagesDeletedEvent(toDeleteUrls));
    }

    public <T> void publishSensitiveImagesModifyEvent(
        Collection<T> oldImages,
        List<String> newImages,
        Function<T, String> extractor
    ) {
        List<String> oldImageUrls = extractUrls(oldImages, extractor);
        List<String> toDeleteUrls = extractDeleteUrls(newImages, oldImageUrls);
        eventPublisher.publishEvent(new ImagesSensitiveDeletedEvent(toDeleteUrls));
    }

    public <T> void publishImagesDeletedEvent(Collection<T> images, Function<T, String> extractor) {
        List<String> imageUrls = extractUrls(images, extractor);
        eventPublisher.publishEvent(new ImagesDeletedEvent(imageUrls));
    }

    public <T> void publishSensitiveImagesDeletedEvent(Collection<T> images, Function<T, String> extractor) {
        List<String> imageUrls = extractUrls(images, extractor);
        eventPublisher.publishEvent(new ImagesSensitiveDeletedEvent(imageUrls));
    }

    /*
        아래 4개의 메소드는 추후 인자 T image, Function<T, String> extractor로 변경 필요
    */
    public void publishImageModifyEvent(String oldImage, String newImage) {
        if (oldImage.equals(newImage)) return;
        eventPublisher.publishEvent(new ImageDeletedEvent(oldImage));
    }

    public void publishSensitiveImageModifyEvent(String oldImage, String newImage) {
        if (oldImage.equals(newImage)) return;
        eventPublisher.publishEvent(new ImageSensitiveDeletedEvent(oldImage));
    }

    public void publishImageDeletedEvent(String image) {
        eventPublisher.publishEvent(new ImageDeletedEvent(image));
    }

    public <T> void publishSensitiveImageDeletedEvent(String image) {
        eventPublisher.publishEvent(new ImageSensitiveDeletedEvent(image));
    }

    private <T> List<String> extractUrls(Collection<T> images, Function<T, String> extractor) {
        return images.stream()
            .map(extractor)
            .toList();
    }

    private static List<String> extractDeleteUrls(List<String> newImages, List<String> oldImageUrls) {
        Set<String> newImagesSet = new HashSet<>(newImages);
        return oldImageUrls.stream()
            .filter(url -> !newImagesSet.contains(url))
            .toList();
    }
}
