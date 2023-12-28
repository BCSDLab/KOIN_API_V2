package in.koreatech.koin.domain.dept.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.dept.domain.Dept;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.repository.DeptRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptRepository deptRepository;

    public DeptResponse findDeptBy(Long id) {
        Dept deptWithoutNumber = deptRepository.findByDeptNumNumber(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부 코드입니다."));
        return DeptResponse.from(deptWithoutNumber, id);
    }
}
