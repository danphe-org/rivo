package com.danphe.rivo.repository.api;

import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.common.user.UserRoleDomain;
import java.util.List;

public interface UserRoleRepository extends AbstractDao<UserRoleDomain, UserFilter> {

  List<UserRoleDomain> findByUserId(String userId);

  List<UserRoleDomain> findAllByUserIdIn(List<String> userIds);

  int deleteByUserId(String userId);
}
