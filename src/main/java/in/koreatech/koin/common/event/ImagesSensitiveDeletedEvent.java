package in.koreatech.koin.common.event;

import java.util.List;

public record ImagesSensitiveDeletedEvent(
    List<String> imageUrls
) {

}
