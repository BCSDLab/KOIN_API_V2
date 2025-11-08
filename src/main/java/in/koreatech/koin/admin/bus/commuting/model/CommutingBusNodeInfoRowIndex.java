package in.koreatech.koin.admin.bus.commuting.model;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_NODE_INFO_END_POINT;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_NODE_INFO_START_POINT;

import java.util.Optional;

import in.koreatech.koin.global.exception.CustomException;

public record CommutingBusNodeInfoRowIndex(
    Integer startRowIndex,
    Integer endRowIndex
) {
    public static CommutingBusNodeInfoRowIndex from(
        Optional<Integer> startRowIndex,
        Optional<Integer> endRowIndex
    ) {
        int start = startRowIndex.orElseThrow(() -> CustomException.of(INVALID_NODE_INFO_START_POINT));
        int end = endRowIndex.orElseThrow(() -> CustomException.of(INVALID_NODE_INFO_END_POINT));
        return new CommutingBusNodeInfoRowIndex(start, end);
    }
}
