package com.danphe.rivo.repository.api;

import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdata.MasterDataFilter;
import java.util.List;
import java.util.Optional;

public interface MasterDataRepository
    extends AbstractDao<MasterDataDomain, MasterDataFilter> {

  List<MasterDataDomain> findByCode(String code);

  Optional<MasterDataDomain> findByCodeAndTitle(String code, String title);
}
