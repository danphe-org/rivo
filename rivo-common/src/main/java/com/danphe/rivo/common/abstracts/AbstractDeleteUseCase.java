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

public abstract class AbstractDeleteUseCase<D extends Domain, O> implements UseCase<D, O> {

  protected final EntityConverter converter;
  protected final AbstractDao entityDao;
  protected final AbstractDao auditDao;

  protected AbstractDeleteUseCase(
      EntityConverter converter, AbstractDao auditDao, AbstractDao entityDao) {
    this.converter = converter;
    this.auditDao = auditDao;
    this.entityDao = entityDao;
  }

  @Override
  public O execute(D req) {
    this.validate(req);
    D domain = this.prepareFilter(req);
    D oldDomain = this.getOldDomain(domain);
    this.preProcessing(domain, oldDomain);
    AuditDomain auditDomain = this.prepareAuditDomain(domain, oldDomain);
    auditDomain.setResource(RequestContextHolder.isPresent() ? RequestContextHolder.get().getResource() : null);
    this.insertIntoAuditLog(auditDomain);
    this.update(domain);
    this.postSuccess(domain, oldDomain);
    return prepareResponse(domain);
  }

  protected void validate(D filter) {}

  protected void preProcessing(D filter, D oldDomain) {}

  protected void postSuccess(D filter, D oldDomain) {}

  protected AuditDomain prepareAuditDomain(D domain, D oldDomain) {
    AuditDomain auditDomain = new AuditDomain();
    auditDomain.setId(StringUtils.generateUUID());
    auditDomain.setEntityKey(domain.getId());
    auditDomain.setEntity(this.getEntity());
    auditDomain.setOldEntity(Jsons.toJsonObj(oldDomain));
    auditDomain.setRequestedBy(getUser());
    auditDomain.setRequestedOn(LocalDateTime.now());
    auditDomain.setRespondedBy(getUser());
    auditDomain.setRespondedOn(LocalDateTime.now());
    auditDomain.setActive(false);
    auditDomain.setDeleted(true);
    auditDomain.setRequestType(RequestType.DELETE.name());
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

  protected abstract O prepareResponse(D domain);

  protected D prepareFilter(D domain) {
    domain.setDeleted(true);
    domain.setActive(false);
    domain.setDeletedOn(LocalDateTime.now());
    domain.setDeletedBy(getUser());
    return domain;
  }

  protected abstract String getEntity();

  protected String getUser() {
    return RequestContextHolder.isPresent() ? RequestContextHolder.get().getSubject() : "SYSTEM";
  }
}
