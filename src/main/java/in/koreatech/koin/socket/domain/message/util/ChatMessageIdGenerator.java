package in.koreatech.koin.socket.domain.message.util;
import org.springframework.stereotype.Component;

import com.github.f4b6a3.tsid.TsidCreator;

@Component
public class ChatMessageIdGenerator {

    public Long generate() {
        return TsidCreator.getTsid256().toLong();
    }
}
