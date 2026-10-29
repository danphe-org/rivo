package com.danphe.rivo.service.api;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdata.MasterDataFilter;
import com.danphe.rivo.common.user.UserDomain;
import java.util.List;

public interface MasterDataService {

  MasterDataDomain insert(MasterDataDomain domain, UserDomain actor);

  MasterDataDomain getById(String id, UserDomain actor);

  List<MasterDataDomain> getByCode(String code, UserDomain actor);

  MasterDataDomain update(MasterDataDomain domain, UserDomain actor);

  void delete(String id, UserDomain actor);

  Page<MasterDataDomain> search(MasterDataFilter filter, UserDomain actor);
}
