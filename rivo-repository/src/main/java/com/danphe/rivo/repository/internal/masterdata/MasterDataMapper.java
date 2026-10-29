package com.danphe.rivo.repository.internal.masterdata;

import com.danphe.rivo.common.masterdata.MasterDataFilter;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MasterDataMapper {

  int insert(MasterDataEntity entity);

  int update(MasterDataEntity entity);

  int delete(MasterDataEntity entity);

  MasterDataEntity findById(@Param("id") String id);

  MasterDataEntity findByCode(@Param("code") String code);

  List<MasterDataEntity> findAll(@Param("filter") MasterDataFilter filter);

  List<MasterDataEntity> findAllWithoutPagination(@Param("filter") MasterDataFilter filter);

  int countAll(@Param("filter") MasterDataFilter filter);
}
