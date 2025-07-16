package in.koreatech.koin.domain.community.lostitem.chatmessage.util;
import org.springframework.stereotype.Component;

import com.github.f4b6a3.tsid.TsidCreator;

@Component
public class ChatMessageIdGenerator {

    public Long generate() {
        return TsidCreator.getTsid256().toLong();
    }
}
