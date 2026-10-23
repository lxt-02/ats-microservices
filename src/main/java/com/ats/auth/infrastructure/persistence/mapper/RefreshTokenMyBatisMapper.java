package com.ats.auth.infrastructure.persistence.mapper;

import com.ats.auth.infrastructure.persistence.record.RefreshTokenRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.Optional;

@Mapper
public interface RefreshTokenMyBatisMapper {

    int insert(RefreshTokenRecord record);

    int update(RefreshTokenRecord record);

    /** SELECT ... FOR UPDATE */
    Optional<RefreshTokenRecord> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    int revokeAllActiveByUserId(@Param("userId") Long userId,
                                @Param("reason") String reason,
                                @Param("now") Instant now);
}
