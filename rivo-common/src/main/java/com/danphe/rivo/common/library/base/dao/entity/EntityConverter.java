package com.danphe.rivo.common.library.base.dao.entity;

import com.danphe.rivo.common.library.base.service.api.Domain;
import java.util.List;

public interface EntityConverter<D extends Domain, E extends Entity> extends Converter {

  E toEntity(D domain);

  D toDomain(E entity);

  List<D> toDomainList(List<E> entities);
}
