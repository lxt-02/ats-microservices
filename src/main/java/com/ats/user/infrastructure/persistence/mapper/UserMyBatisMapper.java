package com.ats.user.infrastructure.persistence.mapper;

import com.ats.user.infrastructure.persistence.record.UserRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMyBatisMapper {

    int insert(UserRecord record);

    int update(UserRecord record);

    Optional<UserRecord> findActiveById(@Param("id") Long id);

    Optional<UserRecord> findActiveByEmail(@Param("email") String email);

    Optional<UserRecord> findActiveByExternalIdentity(@Param("authProvider") String authProvider,
                                                      @Param("externalSubjectId") String externalSubjectId);

    boolean existsActiveByEmail(@Param("email") String email);

    long countActive(@Param("keyword") String keyword, @Param("role") String role, @Param("status") String status);

    List<UserRecord> searchActive(@Param("keyword") String keyword, @Param("role") String role,
                                  @Param("status") String status, @Param("limit") int limit,
                                  @Param("offset") long offset);
}
