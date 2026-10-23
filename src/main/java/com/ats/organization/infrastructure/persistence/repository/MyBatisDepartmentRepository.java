package com.ats.organization.infrastructure.persistence.repository;

import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.repository.DepartmentRepository;
import com.ats.organization.domain.valueobject.DepartmentId;
import com.ats.organization.infrastructure.persistence.converter.DepartmentPersistenceConverter;
import com.ats.organization.infrastructure.persistence.mapper.DepartmentMyBatisMapper;
import com.ats.organization.infrastructure.persistence.record.DepartmentRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisDepartmentRepository implements DepartmentRepository {

    private final DepartmentMyBatisMapper mapper;

    public MyBatisDepartmentRepository(DepartmentMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Department save(Department department) {
        DepartmentRecord record = DepartmentPersistenceConverter.toRecord(department);
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.update(record);
        }
        return DepartmentPersistenceConverter.toAggregate(record);
    }

    @Override
    public Optional<Department> findById(DepartmentId id) {
        return mapper.findActiveById(id.value()).map(DepartmentPersistenceConverter::toAggregate);
    }

    @Override
    public List<Department> findByParentId(DepartmentId parentId) {
        return mapper.findActiveByParentId(parentId == null ? null : parentId.value())
                .stream()
                .map(DepartmentPersistenceConverter::toAggregate)
                .toList();
    }
}
