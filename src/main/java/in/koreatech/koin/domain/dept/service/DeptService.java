package in.koreatech.koin.domain.dept.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.repository.DeptRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptRepository deptRepository;

    public DeptResponse findDeptBy(Long id) {
        return DeptResponse.from(deptRepository.findDeptBy(id));
    }
}
