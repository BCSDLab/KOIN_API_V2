package in.koreatech.koin.domain.dept.service;

import static in.koreatech.koin.domain.dept.model.Dept.NEW_ENERGY_MATERIALS_CHEMICAL_ENGINEERING;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.dto.DeptsResponse;
import in.koreatech.koin.domain.dept.model.Dept;

@Service
public class DeptService {

    public DeptResponse getById(String id) {
        Optional<Dept> dept = Dept.findByNumber(id);

        return dept.map(value -> DeptResponse.from(id, value)).orElse(null);
    }

    public List<DeptsResponse> getAll() {
        return Dept.findAll()
            .stream()
            .filter(dept -> !NEW_ENERGY_MATERIALS_CHEMICAL_ENGINEERING.equals(dept))
            .map(DeptsResponse::from)
            .toList();
    }
}
