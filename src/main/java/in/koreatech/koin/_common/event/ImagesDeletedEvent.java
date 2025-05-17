package in.koreatech.koin._common.event;

import java.util.List;

public record ImagesDeletedEvent(
    List<String> imageUrls
) {

}
