package in.koreatech.koin.domain.dept.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.dept.dto.DeptListItemResponse;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.model.Dept;

@Service
public class DeptService {

    public DeptResponse getById(Long id) {
        Optional<Dept> dept = Dept.findByNumber(id);

        return dept.map(value -> DeptResponse.from(id, value)).orElse(null);
    }

    public List<DeptListItemResponse> getAll() {
        return Dept.findAll()
            .stream()
            .map(DeptListItemResponse::from)
            .toList();
    }
}
