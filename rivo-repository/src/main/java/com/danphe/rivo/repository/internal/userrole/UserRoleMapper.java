package com.danphe.rivo.repository.internal.userrole;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper {

  int insert(UserRoleEntity entity);

  int update(UserRoleEntity entity);

  int delete(UserRoleEntity entity);

  int deleteByUserId(@Param("userId") String userId);

  UserRoleEntity findById(@Param("id") String id);

  List<UserRoleEntity> findByUserId(@Param("userId") String userId);

  List<UserRoleEntity> findAllByUserIdIn(@Param("userIds") List<String> userIds);
}
