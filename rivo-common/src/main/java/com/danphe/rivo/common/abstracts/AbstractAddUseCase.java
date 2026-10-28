package com.danphe.rivo.common.abstracts;

import com.danphe.rivo.common.audits.AuditDomain;
import com.danphe.rivo.common.constants.RequestType;
import com.danphe.rivo.common.context.RequestContextHolder;
import com.danphe.rivo.common.helper.Jsons;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.service.api.Domain;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.common.library.util.string.StringUtils;
import java.time.LocalDateTime;

public abstract class AbstractAddUseCase<D extends Domain, O> implements UseCase<D, O> {

  protected final EntityConverter converter;
  protected final AbstractDao entityDao;
  protected final AbstractDao auditDao;

  protected AbstractAddUseCase(
      EntityConverter converter, AbstractDao auditDao, AbstractDao entityDao) {
    this.converter = converter;
    this.auditDao = auditDao;
    this.entityDao = entityDao;
  }

  @Override
  public O execute(D req) {
    this.validate(req);
    D domain = this.prepareDomain(req);
    this.preProcessing(domain);
    AuditDomain auditDomain = this.prepareAuditDomain(domain);
    auditDomain.setResource(RequestContextHolder.isPresent() ? RequestContextHolder.get().getResource() : null);
    this.insertIntoAuditLog(auditDomain);
    this.insert(domain);
    this.postSuccess(domain);
    return prepareResponse(domain);
  }

  protected void validate(D domain) {}

  protected void preProcessing(D domain) {}

  protected AuditDomain prepareAuditDomain(D domain) {
    AuditDomain auditDomain = new AuditDomain();
    auditDomain.setId(StringUtils.generateUUID());
    auditDomain.setEntityKey(domain.getId());
    auditDomain.setEntity(this.getEntity());
    auditDomain.setNewEntity(Jsons.toJsonObj(domain));
    auditDomain.setRequestedBy(getUser());
    auditDomain.setRequestedOn(LocalDateTime.now());
    auditDomain.setRespondedBy(getUser());
    auditDomain.setRespondedOn(LocalDateTime.now());
    auditDomain.setActive(true);
    auditDomain.setDeleted(false);
    auditDomain.setRequestType(RequestType.ADD.name());
    return auditDomain;
  }

  protected void insertIntoAuditLog(AuditDomain auditDomain) {
    if (this.auditDao != null) {
      this.auditDao.insert(auditDomain);
    }
  }

  protected int insert(D domain) {
    return this.entityDao.insert(domain);
  }

  protected void postSuccess(D domain) {}

  protected abstract O prepareResponse(D domain);

  protected D prepareDomain(D domain) {
    if (domain.getId() == null || domain.getId().isBlank()) {
      domain.setId(StringUtils.generateUUID());
    }
    domain.setCreatedOn(LocalDateTime.now());
    domain.setLastModifiedOn(LocalDateTime.now());
    domain.setCreatedBy(getUser());
    domain.setLastModifiedBy(getUser());
    domain.setActive(true);
    domain.setDeleted(false);
    domain.setVersion(1L);
    return domain;
  }

  protected abstract String getEntity();

  protected String getUser() {
    return RequestContextHolder.isPresent() ? RequestContextHolder.get().getSubject() : "SYSTEM";
  }
}
