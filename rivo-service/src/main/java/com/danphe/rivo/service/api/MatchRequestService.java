package com.danphe.rivo.service.api;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestFilter;
import com.danphe.rivo.common.user.UserDomain;

public interface MatchRequestService {

  MatchRequestDomain insert(MatchRequestDomain domain, UserDomain actor);

  MatchRequestDomain getById(String id, UserDomain actor);

  MatchRequestDomain update(MatchRequestDomain domain, UserDomain actor);

  void delete(String id, UserDomain actor);

  Page<MatchRequestDomain> search(MatchRequestFilter filter, UserDomain actor);
}
