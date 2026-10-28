package com.danphe.rivo.repository.api;

import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserFilter;
import java.util.Optional;

public interface UserRepository extends AbstractDao<UserDomain, UserFilter> {

  Optional<UserDomain> findByEmail(String email);

  Optional<UserDomain> findByPhone(String phone);
}
