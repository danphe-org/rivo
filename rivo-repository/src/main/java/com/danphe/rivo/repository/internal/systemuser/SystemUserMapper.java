package com.danphe.rivo.repository.internal.systemuser;

import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.repository.internal.userrole.UserRoleEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemUserMapper {

  int insert(SystemUserEntity entity);

  int update(SystemUserEntity entity);

  int delete(SystemUserEntity entity);

  SystemUserEntity findById(@Param("id") String id);

  SystemUserEntity findByEmail(@Param("email") String email);

  SystemUserEntity findByPhone(@Param("phone") String phone);

  List<SystemUserEntity> findAll(@Param("filter") UserFilter filter);

  List<SystemUserEntity> findAllWithoutPagination(@Param("filter") UserFilter filter);

  int countAll(@Param("filter") UserFilter filter);

  int insertRole(UserRoleEntity role);

  int deleteRolesByUserId(@Param("userId") String userId);

  List<UserRoleEntity> findRolesByUserId(@Param("userId") String userId);

  List<UserRoleEntity> findAllRolesByUserIds(@Param("userIds") List<String> userIds);
}
