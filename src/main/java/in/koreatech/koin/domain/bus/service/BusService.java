package in.koreatech.koin.domain.bus.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    public BusRemainTimeResponse getBusRemainTime(String busType, String depart, String arrival) {
        return null;
    }
}
