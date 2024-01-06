package in.koreatech.koin.domain.dept.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dept.dto.DeptListItemResponse;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.model.Dept;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeptService {

    public DeptResponse findById(Long id) {
        Dept dept = Dept.findByNumber(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부 코드입니다."));

        return DeptResponse.from(id, dept);
    }

    public List<DeptListItemResponse> findAll() {
        return Dept.findAll()
            .stream()
            .map(DeptListItemResponse::from)
            .toList();
    }
}
