package com.danphe.rivo.service.api;

public interface ClientUserService extends UserService {

  void markVerified(String userId);
}
