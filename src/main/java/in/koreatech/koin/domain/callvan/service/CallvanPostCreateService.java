package in.koreatech.koin.domain.callvan.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateResponse;
import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.enums.CallvanRole;
import in.koreatech.koin.domain.callvan.repository.CallvanChatRoomRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanPostCreateService {

    private final CallvanPostRepository callvanPostRepository;
    private final CallvanParticipantRepository callvanParticipantRepository;
    private final CallvanChatRoomRepository callvanChatRoomRepository;
    private final UserRepository userRepository;
    private final CallvanNotificationScheduler callvanNotificationScheduler;

    @Transactional
    public CallvanPostCreateResponse createCallvanPost(CallvanPostCreateRequest request, Integer userId) {
        User user = userRepository.getById(userId);

        validateLocation(request.departureType(), request.departureCustomName());
        validateLocation(request.arrivalType(), request.arrivalCustomName());

        CallvanPost callvanPost = CallvanPost.builder()
                .author(user)
                .title(generateTitle(request))
                .departureType(request.departureType())
                .departureCustomName(getLocationName(request.departureType(), request.departureCustomName()))
                .arrivalType(request.arrivalType())
                .arrivalCustomName(getLocationName(request.arrivalType(), request.arrivalCustomName()))
                .departureDate(request.departureDate())
                .departureTime(request.departureTime())
                .maxParticipants(request.maxParticipants())
                .build();
        callvanPostRepository.save(callvanPost);

        CallvanParticipant callvanParticipant = CallvanParticipant.builder()
                .post(callvanPost)
                .member(user)
                .role(CallvanRole.AUTHOR)
                .build();
        callvanParticipantRepository.save(callvanParticipant);

        CallvanChatRoom callvanChatRoom = CallvanChatRoom.builder()
                .roomName(generateCallvanChatRoomRoomName(request))
                .build();
        callvanChatRoom.determineCallvanPost(callvanPost);
        callvanChatRoomRepository.save(callvanChatRoom);

        callvanNotificationScheduler.scheduleNotification(callvanPost);
        return CallvanPostCreateResponse.from(callvanPost);
    }

    private void validateLocation(CallvanLocation type, String customName) {
        if (type != CallvanLocation.CUSTOM && customName != null && !customName.isBlank()) {
            throw CustomException.of(ApiResponseCode.INVALID_CUSTOM_LOCATION_NAME);
        }
        if (type == CallvanLocation.CUSTOM && (customName == null || customName.isBlank())) {
            throw CustomException.of(ApiResponseCode.INVALID_CUSTOM_LOCATION_NAME);
        }
    }

    private String getLocationName(CallvanLocation type, String customName) {
        if (type == CallvanLocation.CUSTOM) {
            return customName;
        }
        return type.getName();
    }

    private String generateTitle(CallvanPostCreateRequest request) {
        String departure = getLocationName(request.departureType(), request.departureCustomName());
        String arrival = getLocationName(request.arrivalType(), request.arrivalCustomName());
        return String.format("%s -> %s", departure, arrival);
    }

    private String generateCallvanChatRoomRoomName(CallvanPostCreateRequest request) {
        return generateTitle(request) + " " + request.departureTime();
    }
}
