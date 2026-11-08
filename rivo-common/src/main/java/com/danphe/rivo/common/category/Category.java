package com.danphe.rivo.common.category;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Category {
  E_SPORTS("E_SPORTS", "E-Sports", "e-sports", "Electronic sports and competitive video gaming", "ESPORTS_GAMES"),
  SPORTS("SPORTS", "Sports", "sports", "Physical outdoor and indoor sports", "SPORTS_GAMES"),
  TRAVEL("TRAVEL", "Travel", "travel", "Travel, outdoor adventures, and tours", "TRAVEL_ACTIVITIES");

  private final String code;
  private final String name;
  private final String slug;
  private final String description;
  private final String masterDataCode;

  Category(String code, String name, String slug, String description, String masterDataCode) {
    this.code = code;
    this.name = name;
    this.slug = slug;
    this.description = description;
    this.masterDataCode = masterDataCode;
  }

  public static Category fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String trimmed = value.trim();
    String normalized = trimmed.toUpperCase().replace("-", "_").replace(" ", "_");
    for (Category category : values()) {
      if (category.name().equals(normalized)
          || category.code.equalsIgnoreCase(trimmed)
          || category.name.equalsIgnoreCase(trimmed)
          || category.slug.equalsIgnoreCase(trimmed)) {
        return category;
      }
    }
    return null;
  }
}
