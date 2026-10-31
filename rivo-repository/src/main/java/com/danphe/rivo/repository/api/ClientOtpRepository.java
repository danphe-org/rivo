package com.danphe.rivo.repository.api;

import com.danphe.rivo.common.otp.OtpDomain;
import java.util.Optional;

public interface ClientOtpRepository {

  int insert(OtpDomain domain);

  int update(OtpDomain domain);

  Optional<OtpDomain> findLatestActiveByIdentifierAndPurpose(String identifier, String purpose);

  void invalidateExistingByIdentifierAndPurpose(String identifier, String purpose);
}
