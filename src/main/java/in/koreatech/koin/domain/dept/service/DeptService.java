package in.koreatech.koin.domain.dept.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.dept.domain.DeptInfo;
import in.koreatech.koin.domain.dept.domain.DeptNum;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.dto.DeptsResponse;
import in.koreatech.koin.domain.dept.repository.DeptNumRepository;
import in.koreatech.koin.domain.dept.repository.DeptInfoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptInfoRepository deptInfoRepository;
    private final DeptNumRepository deptNumRepository;

    public DeptResponse findDeptBy(Long id) {
        DeptNum deptWithoutNumber = deptNumRepository.findByNumber(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부 코드입니다."));

        return DeptResponse.from(deptWithoutNumber);
    }

    public List<DeptsResponse> findAllDept() {
        List<DeptInfo> response = deptInfoRepository.findAll();

        return response.stream()
            .map(DeptsResponse::from)
            .toList();
    }
}
