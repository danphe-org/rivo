package com.danphe.rivo.repository.internal.user;

import com.danphe.rivo.common.user.UserFilter;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

  int insert(UserEntity entity);

  int update(UserEntity entity);

  int delete(UserEntity entity);

  UserEntity findById(@Param("id") String id);

  UserEntity findByEmail(@Param("email") String email);

  UserEntity findByPhone(@Param("phone") String phone);

  List<UserEntity> findAll(@Param("filter") UserFilter filter);

  List<UserEntity> findAllWithoutPagination(@Param("filter") UserFilter filter);

  int countAll(@Param("filter") UserFilter filter);
}
