package com.ats.auth.infrastructure.persistence.mapper;

import com.ats.auth.infrastructure.persistence.record.SsoConfigurationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface SsoConfigurationMyBatisMapper {

    int insert(SsoConfigurationRecord record);

    int update(SsoConfigurationRecord record);

    Optional<SsoConfigurationRecord> findActiveById(@Param("id") Long id);

    /** First enabled, not deleted configuration of the provider. */
    Optional<SsoConfigurationRecord> findEnabledByProviderType(@Param("providerType") String providerType);
}
