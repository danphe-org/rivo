package com.danphe.rivo.service.api;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemFilter;
import com.danphe.rivo.common.user.UserDomain;
import java.util.List;

public interface MasterDataItemService {

  MasterDataItemDomain insert(MasterDataItemDomain domain, UserDomain actor);

  MasterDataItemDomain getById(String id, UserDomain actor);

  List<MasterDataItemDomain> getByMasterDataId(String masterDataId, UserDomain actor);

  MasterDataItemDomain update(MasterDataItemDomain domain, UserDomain actor);

  void delete(String id, UserDomain actor);

  Page<MasterDataItemDomain> search(MasterDataItemFilter filter, UserDomain actor);
}
