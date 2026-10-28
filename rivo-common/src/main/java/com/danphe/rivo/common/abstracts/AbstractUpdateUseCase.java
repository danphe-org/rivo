package com.danphe.rivo.common.abstracts;

import com.danphe.rivo.common.audits.AuditDomain;
import com.danphe.rivo.common.constants.RequestType;
import com.danphe.rivo.common.context.RequestContextHolder;
import com.danphe.rivo.common.helper.Jsons;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.base.service.api.Domain;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.common.library.util.string.StringUtils;
import java.time.LocalDateTime;

public abstract class AbstractUpdateUseCase<D extends Domain, O> implements UseCase<D, O> {

  protected final EntityConverter converter;
  protected final AbstractDao entityDao;
  protected final AbstractDao auditDao;

  protected AbstractUpdateUseCase(
      EntityConverter converter, AbstractDao auditDao, AbstractDao entityDao) {
    this.converter = converter;
    this.auditDao = auditDao;
    this.entityDao = entityDao;
  }

  @Override
  public O execute(D req) {
    this.validate(req);
    D domain = this.prepareDomain(req);
    D oldDomain = this.getOldDomain(domain);
    domain.setVersion(oldDomain.getVersion() != null ? oldDomain.getVersion() + 1 : 1L);
    this.preProcessing(domain, oldDomain);
    AuditDomain auditDomain = this.prepareAuditDomain(domain, oldDomain);
    auditDomain.setResource(RequestContextHolder.isPresent() ? RequestContextHolder.get().getResource() : null);
    this.insertIntoAuditLog(auditDomain);
    this.update(domain);
    this.postSuccess(domain, oldDomain);
    return prepareResponse(domain);
  }

  protected void validate(D domain) {}

  protected void preProcessing(D domain, D oldDomain) {}

  protected AuditDomain prepareAuditDomain(D domain, D oldDomain) {
    AuditDomain auditDomain = new AuditDomain();
    auditDomain.setId(StringUtils.generateUUID());
    auditDomain.setEntityKey(domain.getId());
    auditDomain.setEntity(this.getEntity());
    auditDomain.setOldEntity(Jsons.toJsonObj(oldDomain));
    auditDomain.setNewEntity(Jsons.toJsonObj(domain));
    auditDomain.setRequestedBy(getUser());
    auditDomain.setRequestedOn(LocalDateTime.now());
    auditDomain.setRespondedBy(getUser());
    auditDomain.setRespondedOn(LocalDateTime.now());
    auditDomain.setActive(true);
    auditDomain.setDeleted(false);
    auditDomain.setRequestType(RequestType.UPDATE.name());
    return auditDomain;
  }

  protected void insertIntoAuditLog(AuditDomain auditDomain) {
    if (this.auditDao != null) {
      this.auditDao.insert(auditDomain);
    }
  }

  protected int update(D domain) {
    return this.entityDao.update(domain);
  }

  @SuppressWarnings("unchecked")
  protected D getOldDomain(D domain) {
    java.util.Optional<?> opt = this.entityDao.findById(domain.getId());
    return (D) opt.orElseThrow(() -> new NotFoundException("Entity not found: " + domain.getId()));
  }

  protected void postSuccess(D domain, D oldDomain) {}

  protected abstract O prepareResponse(D domain);

  protected D prepareDomain(D domain) {
    domain.setLastModifiedOn(LocalDateTime.now());
    domain.setLastModifiedBy(getUser());
    return domain;
  }

  protected abstract String getEntity();

  protected String getUser() {
    return RequestContextHolder.isPresent() ? RequestContextHolder.get().getSubject() : "SYSTEM";
  }
}
