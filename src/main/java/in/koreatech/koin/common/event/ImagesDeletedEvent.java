package in.koreatech.koin.common.event;

import java.util.List;

public record ImagesDeletedEvent(
    List<String> imageUrls
) {

}
