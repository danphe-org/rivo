package com.danphe.rivo.service.api;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserFilter;
import java.util.List;
import java.util.Optional;

public interface UserService {

  UserDomain insert(UserDomain domain);

  UserDomain insert(UserDomain domain, UserDomain actor);

  UserDomain register(UserDomain domain);

  UserDomain update(UserDomain domain, UserDomain actor);

  void delete(String id, UserDomain actor);

  Optional<UserDomain> findById(String id);

  UserDomain getById(String id, UserDomain actor);

  Optional<UserDomain> findByEmail(String email);

  Optional<UserDomain> findByPhone(String phone);

  Optional<UserDomain> findOne(UserFilter filter);

  List<UserDomain> findAllWithoutPagination(UserFilter filter);

  Page<UserDomain> findAll(UserFilter filter);

  Page<UserDomain> findAll(UserFilter filter, UserDomain actor);
}
