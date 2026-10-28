package com.danphe.rivo.common.context;

public interface RequestContext {

  String getId();

  String getSubject();

  String getIp();

  String getSessionId();

  String getResource();
}
