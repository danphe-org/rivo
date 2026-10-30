package com.danphe.rivo.repository.api;

import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemFilter;
import java.util.List;
import java.util.Optional;

public interface MasterDataItemRepository
    extends AbstractDao<MasterDataItemDomain, MasterDataItemFilter> {

  List<MasterDataItemDomain> findByMasterDataId(String masterDataId);

  Optional<MasterDataItemDomain> findByMasterDataIdAndTitle(String masterDataId, String title);
}
