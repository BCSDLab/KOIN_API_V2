package in.koreatech.koin.admin.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.member.dto.AdminTrackResponse;
import in.koreatech.koin.admin.member.repository.AdminTrackRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTrackService {

    private final AdminTrackRepository adminTrackRepository;

    public List<AdminTrackResponse> getTracks() {
        return adminTrackRepository.findAll().stream()
            .map(AdminTrackResponse::from)
            .toList();
    }
}
