package in.koreatech.koin.socket.domain.message.service.implement;

import org.springframework.stereotype.Component;

import in.koreatech.koin.socket.domain.message.model.ChatMessageEntity;
import in.koreatech.koin.socket.domain.message.repository.ChatMessageRedisRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageAppender {

    private final ChatMessageRedisRepository chatMessageRedisRepository;

    public void save(ChatMessageEntity message) {
        chatMessageRedisRepository.save(message);
    }
}
