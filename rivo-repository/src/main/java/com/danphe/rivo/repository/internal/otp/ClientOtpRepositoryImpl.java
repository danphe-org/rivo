package com.danphe.rivo.repository.internal.otp;

import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.otp.OtpDomain;
import com.danphe.rivo.repository.api.ClientOtpRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository("clientOtpRepository")
public class ClientOtpRepositoryImpl implements ClientOtpRepository {

  private final ClientOtpMapper mapper;
  private final ClientOtpEntityConverter converter;

  public ClientOtpRepositoryImpl(ClientOtpMapper mapper, ClientOtpEntityConverter converter) {
    this.mapper = mapper;
    this.converter = converter;
  }

  @Override
  public int insert(OtpDomain domain) {
    if (domain.getId() == null || domain.getId().isBlank()) {
      domain.setId(StringUtils.generateUUID());
    }
    if (domain.getCreatedOn() == null) {
      domain.setCreatedOn(LocalDateTime.now());
    }
    if (domain.getLastModifiedOn() == null) {
      domain.setLastModifiedOn(LocalDateTime.now());
    }
    return mapper.insert(converter.toEntity(domain));
  }

  @Override
  public int update(OtpDomain domain) {
    domain.setLastModifiedOn(LocalDateTime.now());
    return mapper.update(converter.toEntity(domain));
  }

  @Override
  public Optional<OtpDomain> findLatestActiveByIdentifierAndPurpose(String identifier, String purpose) {
    ClientOtpEntity entity = mapper.findLatestActiveByIdentifierAndPurpose(identifier, purpose);
    return Optional.ofNullable(converter.toDomain(entity));
  }

  @Override
  public void invalidateExistingByIdentifierAndPurpose(String identifier, String purpose) {
    mapper.invalidateExistingByIdentifierAndPurpose(identifier, purpose);
  }
}
