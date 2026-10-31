package com.danphe.rivo.repository.internal.otp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientOtpMapper {

  int insert(ClientOtpEntity entity);

  int update(ClientOtpEntity entity);

  ClientOtpEntity findLatestActiveByIdentifierAndPurpose(
      @Param("identifier") String identifier, @Param("purpose") String purpose);

  int invalidateExistingByIdentifierAndPurpose(
      @Param("identifier") String identifier, @Param("purpose") String purpose);
}
