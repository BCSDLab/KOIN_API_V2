package in.koreatech.koin.domain.dept.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dept.model.DeptNum;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.dto.DeptListItemResponse;
import in.koreatech.koin.domain.dept.repository.DeptInfoRepository;
import in.koreatech.koin.domain.dept.repository.DeptNumRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeptService {

    private final DeptInfoRepository deptInfoRepository;
    private final DeptNumRepository deptNumRepository;

    public DeptResponse findById(Long id) {
        DeptNum deptNum = deptNumRepository.findByNumber(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부 코드입니다."));

        return DeptResponse.from(deptNum);
    }

    public List<DeptListItemResponse> findAll() {
        return deptInfoRepository.findAll()
            .stream()
            .map(DeptListItemResponse::from)
            .toList();
    }
}
