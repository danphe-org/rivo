package com.danphe.rivo.repository.api;

import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestFilter;

public interface MatchRequestRepository
    extends AbstractDao<MatchRequestDomain, MatchRequestFilter> {}
