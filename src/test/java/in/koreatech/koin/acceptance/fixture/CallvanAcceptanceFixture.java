package in.koreatech.koin.acceptance.fixture;

import static in.koreatech.koin.domain.callvan.model.enums.CallvanLocation.FRONT_GATE;
import static in.koreatech.koin.domain.callvan.model.enums.CallvanLocation.STATION;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanRole;
import in.koreatech.koin.domain.callvan.repository.CallvanChatRoomRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class CallvanAcceptanceFixture {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CallvanPostRepository callvanPostRepository;
    private final CallvanParticipantRepository callvanParticipantRepository;
    private final CallvanChatRoomRepository callvanChatRoomRepository;

    @Autowired
    public CallvanAcceptanceFixture(
        PasswordEncoder passwordEncoder,
        UserRepository userRepository,
        CallvanPostRepository callvanPostRepository,
        CallvanParticipantRepository callvanParticipantRepository,
        CallvanChatRoomRepository callvanChatRoomRepository
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.callvanPostRepository = callvanPostRepository;
        this.callvanParticipantRepository = callvanParticipantRepository;
        this.callvanChatRoomRepository = callvanChatRoomRepository;
    }

    public User 콜벤_유저(String loginId, String nickname) {
        return userRepository.save(User.builder()
            .loginPw(passwordEncoder.encode("1234"))
            .nickname(nickname)
            .name("테스트용_" + nickname)
            .userType(STUDENT)
            .email(loginId + "@koreatech.ac.kr")
            .loginId(loginId)
            .isAuthed(true)
            .isDeleted(false)
            .build()
        );
    }

    public CallvanPost 콜벤팟(User author, LocalDateTime departureAt) {
        CallvanPost callvanPost = callvanPostRepository.save(
            CallvanPost.builder()
                .author(author)
                .title(FRONT_GATE.getName() + " -> " + STATION.getName())
                .departureType(FRONT_GATE)
                .arrivalType(STATION)
                .departureDate(departureAt.toLocalDate())
                .departureTime(departureAt.toLocalTime())
                .maxParticipants(4)
                .build()
        );

        callvanParticipantRepository.save(
            CallvanParticipant.builder()
                .post(callvanPost)
                .member(author)
                .role(CallvanRole.AUTHOR)
                .build()
        );

        CallvanChatRoom chatRoom = CallvanChatRoom.builder()
            .roomName(callvanPost.getTitle() + " " + departureAt.toLocalTime())
            .build();
        chatRoom.determineCallvanPost(callvanPost);
        callvanChatRoomRepository.save(chatRoom);

        return callvanPost;
    }

    public CallvanPost 완료_처리(CallvanPost callvanPost) {
        callvanPost.closeRecruitment();
        callvanPost.completeRecruitment();
        return callvanPostRepository.save(callvanPost);
    }

    public void 참여(CallvanPost callvanPost, User member) {
        callvanParticipantRepository.save(
            CallvanParticipant.builder()
                .post(callvanPost)
                .member(member)
                .role(CallvanRole.PARTICIPANT)
                .build()
        );
    }
}
