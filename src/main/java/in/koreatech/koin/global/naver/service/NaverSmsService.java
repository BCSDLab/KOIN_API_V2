package in.koreatech.koin.global.naver.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.global.naver.sms.NaverSmsClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NaverSmsService {

    private final NaverSmsClient naverSmsClient;

    public void sendVerificationCode(String certificationCode, String phoneNumber) {
        String content = String.format("[KOIN]본인확인 인증번호는 [%s]입니다.", certificationCode);
        naverSmsClient.sendMessage(content, phoneNumber);
    }
}
