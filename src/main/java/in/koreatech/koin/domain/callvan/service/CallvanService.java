package in.koreatech.koin.domain.callvan.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateResponse;
import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
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
public class CallvanService {

    private final CallvanPostRepository callvanPostRepository;
    private final CallvanParticipantRepository callvanParticipantRepository;
    private final CallvanChatRoomRepository callvanChatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public CallvanPostCreateResponse createCallvanPost(CallvanPostCreateRequest request, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_USER));

        CallvanPost callvanPost = CallvanPost.builder()
                .author(user)
                .title(generateTitle(request))
                .departureType(request.departureType())
                .departureCustomName(request.departureCustomName())
                .arrivalType(request.arrivalType())
                .arrivalCustomName(request.arrivalCustomName())
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

        return CallvanPostCreateResponse.from(callvanPost);
    }

    private String generateTitle(CallvanPostCreateRequest request) {
        String departure = request.departureType().getName();
        if (request.departureCustomName() != null && !request.departureCustomName().isBlank()) {
            departure = request.departureCustomName();
        }
        String arrival = request.arrivalType().getName();
        if (request.arrivalCustomName() != null && !request.arrivalCustomName().isBlank()) {
            arrival = request.arrivalCustomName();
        }
        return String.format("%s -> %s", departure, arrival);
    }

    private String generateCallvanChatRoomRoomName(CallvanPostCreateRequest request) {
        return generateTitle(request) + " " + request.departureTime();
    }
}
