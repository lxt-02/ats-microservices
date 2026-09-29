package com.ats.userservice.infrastructure.adapter;

import com.ats.userservice.domain.model.department.aggregate.Department;
import com.ats.userservice.domain.model.department.valueobject.DepartmentId;
import com.ats.userservice.domain.repository.DepartmentRepository;
import com.ats.userservice.infrastructure.mapper.DepartmentPersistenceMapper;
import com.ats.userservice.infrastructure.persistence.repository.SpringDataDepartmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DepartmentRepositoryAdapter implements DepartmentRepository {

    private final SpringDataDepartmentRepository springDataDepartmentRepository;
    private final DepartmentPersistenceMapper mapper;

    public DepartmentRepositoryAdapter(SpringDataDepartmentRepository springDataDepartmentRepository,
                                       DepartmentPersistenceMapper mapper) {
        this.springDataDepartmentRepository = springDataDepartmentRepository;
        this.mapper = mapper;
    }

    @Override
    public Department save(Department department) {
        return mapper.toDomain(springDataDepartmentRepository.save(mapper.toEntity(department)));
    }

    @Override
    public Optional<Department> findById(DepartmentId id) {
        return springDataDepartmentRepository.findByIdAndDeletedFalse(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<Department> findByParentId(DepartmentId parentId) {
        if (parentId == null) {
            return springDataDepartmentRepository.findByParentIdIsNullAndDeletedFalse()
                    .stream()
                    .map(mapper::toDomain)
                    .toList();
        }
        return springDataDepartmentRepository.findByParentIdAndDeletedFalse(parentId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
