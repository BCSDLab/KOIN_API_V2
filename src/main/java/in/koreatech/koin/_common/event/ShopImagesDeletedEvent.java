package in.koreatech.koin._common.event;

import java.util.List;

public record ShopImagesDeletedEvent(
    List<String> s3Keys
) {

}
