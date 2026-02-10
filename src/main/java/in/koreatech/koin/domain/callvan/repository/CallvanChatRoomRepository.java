package in.koreatech.koin.domain.callvan.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;

public interface CallvanChatRoomRepository extends Repository<CallvanChatRoom, Integer> {

    CallvanChatRoom save(CallvanChatRoom callvanChatRoom);
}
