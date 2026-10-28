package com.danphe.rivo.common.library.util.date;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

  public static synchronized long getTimeAsNanoSecond() {
    return System.nanoTime();
  }

  public static String getTime() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
    return simpleDateFormat.format(new Date());
  }

  public static String getDateString() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    return simpleDateFormat.format(new Date());
  }

  public static String now() {
    return LocalDateTime.now().toString();
  }

  public static String getTodayAsUSFormat() {
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    return now.format(formatter);
  }

  public static String getTodayAsNormal() {
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return now.format(formatter);
  }

  public static String toISODateTime(LocalDateTime localDateTime) {
    return localDateTime == null ? "" : localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }

  public static LocalDateTime toLocalDateTime(String stringDate, String pattern) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return LocalDateTime.parse(stringDate, formatter);
  }
}
