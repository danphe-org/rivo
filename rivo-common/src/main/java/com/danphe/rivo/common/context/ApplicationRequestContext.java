package com.danphe.rivo.common.context;

import lombok.Data;

@Data
public class ApplicationRequestContext implements RequestContext {
  private String id;
  private String subject;
  private String ip;
  private String sessionId;
  private String agent;
  private String resource;
}
