package in.koreatech.koin.domain.community.lostitem.chatroom.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.community.lostitem.chatroom.usecase.LostItemChatUserBlockUseCase;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
public class LostItemChatRoomUserBlockController implements LostItemChatRoomUserBlockApi {

    private final LostItemChatUserBlockUseCase lostItemChatUserBlockUseCase;

    @PostMapping("/lost-item/{articleId}/{chatRoomId}/block")
    public ResponseEntity<?> blockChatUser(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    ) {
        lostItemChatUserBlockUseCase.blockUser(articleId, chatRoomId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/lost-item/{articleId}/{chatRoomId}/unblock")
    public ResponseEntity<?> unblockChatUser(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer studentId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    ) {
        lostItemChatUserBlockUseCase.unblockUser(articleId, chatRoomId, studentId);
        return ResponseEntity.ok().build();
    }
}
