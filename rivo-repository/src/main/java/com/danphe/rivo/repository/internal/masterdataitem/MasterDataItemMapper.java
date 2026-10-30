package com.danphe.rivo.repository.internal.masterdataitem;

import com.danphe.rivo.common.masterdataitem.MasterDataItemFilter;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MasterDataItemMapper {

  int insert(MasterDataItemEntity entity);

  int update(MasterDataItemEntity entity);

  int delete(MasterDataItemEntity entity);

  MasterDataItemEntity findById(@Param("id") String id);

  List<MasterDataItemEntity> findByMasterDataId(@Param("masterDataId") String masterDataId);

  MasterDataItemEntity findByMasterDataIdAndTitle(
      @Param("masterDataId") String masterDataId,
      @Param("title") String title);

  List<MasterDataItemEntity> findAll(@Param("filter") MasterDataItemFilter filter);

  List<MasterDataItemEntity> findAllWithoutPagination(@Param("filter") MasterDataItemFilter filter);

  int countAll(@Param("filter") MasterDataItemFilter filter);
}
