package com.danphe.rivo.repository.internal.clientuser;

import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.repository.internal.userrole.UserRoleEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientUserMapper {

  int insert(ClientUserEntity entity);

  int update(ClientUserEntity entity);

  int delete(ClientUserEntity entity);

  ClientUserEntity findById(@Param("id") String id);

  ClientUserEntity findByEmail(@Param("email") String email);

  ClientUserEntity findByPhone(@Param("phone") String phone);

  List<ClientUserEntity> findAll(@Param("filter") UserFilter filter);

  List<ClientUserEntity> findAllWithoutPagination(@Param("filter") UserFilter filter);

  int countAll(@Param("filter") UserFilter filter);

  int insertRole(UserRoleEntity role);

  int deleteRolesByUserId(@Param("userId") String userId);

  List<UserRoleEntity> findRolesByUserId(@Param("userId") String userId);

  List<UserRoleEntity> findAllRolesByUserIds(@Param("userIds") List<String> userIds);
}
