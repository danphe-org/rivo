package com.danphe.rivo.repository.api;

import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdata.MasterDataFilter;
import java.util.Optional;

public interface MasterDataRepository
    extends AbstractDao<MasterDataDomain, MasterDataFilter> {

  Optional<MasterDataDomain> findByCode(String code);
}
