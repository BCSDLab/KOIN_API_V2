package in.koreatech.koin.domain.dept.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.dept.domain.DeptNum;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.dto.DeptsResponse;
import in.koreatech.koin.domain.dept.repository.DeptInfoRepository;
import in.koreatech.koin.domain.dept.repository.DeptNumRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptInfoRepository deptInfoRepository;
    private final DeptNumRepository deptNumRepository;

    public DeptResponse findDeptBy(Long id) {
        DeptNum deptNum = deptNumRepository.findByNumber(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부 코드입니다."));

        return DeptResponse.from(deptNum);
    }

    public List<DeptsResponse> findAllDept() {
        return deptInfoRepository.findAll()
            .stream()
            .map(DeptsResponse::from)
            .toList();
    }
}
