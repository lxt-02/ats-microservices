package com.ats.userservice.infrastructure.persistence.repository;

import com.ats.userservice.infrastructure.persistence.entity.DepartmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataDepartmentRepository extends JpaRepository<DepartmentJpaEntity, Long> {

    Optional<DepartmentJpaEntity> findByIdAndDeletedFalse(Long id);

    List<DepartmentJpaEntity> findByParentIdAndDeletedFalse(Long parentId);

    List<DepartmentJpaEntity> findByParentIdIsNullAndDeletedFalse();
}
