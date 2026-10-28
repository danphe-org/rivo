package com.danphe.rivo.common.context;

public final class RequestContextHolder {

  private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

  private RequestContextHolder() {}

  public static void set(RequestContext context) {
    HOLDER.set(context);
  }

  public static RequestContext get() {
    RequestContext context = HOLDER.get();
    if (context == null) {
      throw new IllegalStateException("No authenticated user in current thread context");
    }
    return context;
  }

  public static boolean isPresent() {
    return HOLDER.get() != null;
  }

  public static void clear() {
    HOLDER.remove();
  }
}
