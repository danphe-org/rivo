package com.danphe.rivo.repository.internal.matchrequest;

import com.danphe.rivo.common.matchrequest.MatchRequestFilter;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MatchRequestMapper {

  int insert(MatchRequestEntity entity);

  int update(MatchRequestEntity entity);

  int delete(MatchRequestEntity entity);

  MatchRequestEntity findById(@Param("id") String id);

  List<MatchRequestEntity> findAll(@Param("filter") MatchRequestFilter filter);

  List<MatchRequestEntity> findAllWithoutPagination(@Param("filter") MatchRequestFilter filter);

  int countAll(@Param("filter") MatchRequestFilter filter);
}
