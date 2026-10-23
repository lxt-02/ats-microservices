package com.ats.organization.infrastructure.persistence.mapper;

import com.ats.organization.infrastructure.persistence.record.DepartmentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DepartmentMyBatisMapper {

    int insert(DepartmentRecord record);

    int update(DepartmentRecord record);

    Optional<DepartmentRecord> findActiveById(@Param("id") Long id);

    List<DepartmentRecord> findActiveByParentId(@Param("parentId") Long parentId);
}
