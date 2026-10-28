package com.danphe.rivo.common.library.util.string;

import java.util.Arrays;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

  public static boolean isBlankOrNull(String str) {
    return str == null || str.isBlank();
  }

  public static String generateUUID() {
    return UUID.randomUUID().toString();
  }

  public static String capitalize(String value) {
    if (isBlankOrNull(value)) {
      return "";
    } else {
      String[] values = value.split(" ");
      StringBuilder builder = new StringBuilder();
      Arrays.stream(values)
          .forEach(
              (val) -> {
                builder.append(capitalizeWord(val.toLowerCase()));
                builder.append(" ");
              });
      return builder.toString().trim();
    }
  }

  private static String capitalizeWord(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }
}
